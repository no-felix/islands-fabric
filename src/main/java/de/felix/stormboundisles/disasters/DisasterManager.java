package de.felix.stormboundisles.disasters;

import de.felix.stormboundisles.teams.Team;
import de.felix.stormboundisles.teams.TeamManager;
import de.felix.stormboundisles.zones.IslandZone;
import de.felix.stormboundisles.zones.ZoneManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.*;

/**
 * Manages disasters for all teams/islands, including random triggers and cleanup.
 */
public class DisasterManager {
	private static DisasterManager instance;

	// islandType -> disaster definitions
	private final Map<String, List<Disaster>> disastersByIslandType = new HashMap<>();
	// teamName -> currently active disaster
	private final Map<String, ActiveDisaster> activeDisasters = new HashMap<>();
	private final Random random = new Random();

	private DisasterManager() {}

	public static DisasterManager getInstance() {
		if (instance == null) instance = new DisasterManager();
		return instance;
	}

	public void registerDisaster(String islandType, Disaster disaster) {
		disastersByIslandType.computeIfAbsent(islandType, k -> new ArrayList<>()).add(disaster);
	}

	public List<Disaster> getDisastersForIsland(String islandType) {
		return disastersByIslandType.getOrDefault(islandType, Collections.emptyList());
	}

	/**
	 * Triggers a disaster randomly for all active islands, should be called from a scheduled tick (e.g. every minute).
	 */
	public void tick(MinecraftServer server) {
		for (Team team : TeamManager.getInstance().getAllTeams()) {
			String teamName = team.getName();
			if (activeDisasters.containsKey(teamName)) {
				// Check if disaster should end
				ActiveDisaster ad = activeDisasters.get(teamName);
				long now = System.currentTimeMillis();
				if (now > ad.endTimestamp) {
					endDisaster(server, teamName);
				}
			} else {
				// Possibly trigger disaster
				List<Disaster> disasters = getDisastersForIsland(team.getIslandType());
				if (disasters.isEmpty()) continue;

				for (Disaster disaster : disasters) {
					// Calculate per-tick chance based on per-hour chance (assuming tick every 60s)
					double perMinuteChance = disaster.getChancePerHour() / 60.0;
					if (random.nextDouble() < perMinuteChance) {
						startDisaster(server, teamName, disaster);
						break; // Only one disaster per tick
					}
				}
			}
		}
	}

	public void startDisaster(MinecraftServer server, String teamName, Disaster disaster) {
		if (activeDisasters.containsKey(teamName)) return; // Already running

		IslandZone zone = ZoneManager.getInstance().getZoneForTeam(teamName);
		if (zone == null) return;

		ServerWorld world = server.getWorld(ServerWorld.OVERWORLD); // TODO: Support multiple worlds
		List<ServerPlayerEntity> players = getPlayersInZone(world, zone);

		// Apply all effects
		for (DisasterEffect effect : disaster.getEffects()) {
			effect.apply(world, zone, players);
		}
		long endTimestamp = System.currentTimeMillis() + disaster.getDurationSeconds() * 1000L;
		activeDisasters.put(teamName, new ActiveDisaster(disaster, endTimestamp));
		// TODO: Announce start to players
	}

	public void endDisaster(MinecraftServer server, String teamName) {
		ActiveDisaster ad = activeDisasters.remove(teamName);
		if (ad == null) return;

		IslandZone zone = ZoneManager.getInstance().getZoneForTeam(teamName);
		if (zone == null) return;

		ServerWorld world = server.getWorld(ServerWorld.OVERWORLD);
		List<ServerPlayerEntity> players = getPlayersInZone(world, zone);

		// Cleanup all effects
		for (DisasterEffect effect : ad.disaster.getEffects()) {
			effect.cleanup(world, zone, players);
		}
		// TODO: Announce end to players
	}

	public boolean isDisasterActive(String teamName) {
		return activeDisasters.containsKey(teamName);
	}

	public Disaster getActiveDisaster(String teamName) {
		ActiveDisaster ad = activeDisasters.get(teamName);
		return ad != null ? ad.disaster : null;
	}

	public record ActiveDisaster(Disaster disaster, long endTimestamp) {
	}

	/**
	 * Returns all players currently in the given zone.
	 */
	private List<ServerPlayerEntity> getPlayersInZone(ServerWorld world, IslandZone zone) {
		List<ServerPlayerEntity> inZone = new ArrayList<>();
		for (ServerPlayerEntity player : world.getPlayers()) {
			if (zone.isInside(player.getBlockPos())) {
				inZone.add(player);
			}
		}
		return inZone;
	}
}