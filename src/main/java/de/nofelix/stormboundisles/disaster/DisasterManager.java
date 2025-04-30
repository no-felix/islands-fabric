package de.nofelix.stormboundisles.disaster;

import de.nofelix.stormboundisles.config.ConfigManager;
import de.nofelix.stormboundisles.data.DataManager;
import de.nofelix.stormboundisles.data.Island;
import de.nofelix.stormboundisles.data.IslandType;
import de.nofelix.stormboundisles.game.ActionbarNotifier;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

/**
 * Manages the triggering and effects of random disasters on islands based on configured intervals.
 * Disasters are chosen based on island type and apply effects to players within the island's zone.
 */
public class DisasterManager {
	/** Maps island types to the possible disasters that can occur on them. */
	private static final Map<IslandType, DisasterType[]> DEFAULTS = Map.of(
			IslandType.VOLCANO, new DisasterType[]{DisasterType.METEOR},
			IslandType.ICE, new DisasterType[]{DisasterType.BLIZZARD},
			IslandType.DESERT, new DisasterType[]{DisasterType.SANDSTORM},
			IslandType.MUSHROOM, new DisasterType[]{DisasterType.SPORE},
			IslandType.CRYSTAL, new DisasterType[]{DisasterType.CRYSTAL_STORM}
	);

	/** Counter for server ticks to determine when to trigger the next disaster check. */
	private static int ticks = 0;
	/** Set containing keys representing currently active disasters (format: "islandId:DisasterType"). Used to prevent duplicates. */
	private static final Set<String> activeDisasters = new HashSet<>();

	/**
	 * Registers the server tick event listener used for disaster management.
	 */
	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(DisasterManager::onServerTick);
	}

	/**
	 * Triggers a specific disaster on a given island.
	 * Applies effects to players currently within the island's zone and broadcasts a server-wide message.
	 * Prevents triggering the same disaster type if it's already active on the island.
	 * The active disaster flag is removed after a short delay.
	 *
	 * @param server   The Minecraft server instance.
	 * @param islandId The ID of the island where the disaster occurs.
	 * @param type     The type of disaster to trigger.
	 */
	public static void triggerDisaster(MinecraftServer server, String islandId, DisasterType type) {
		Island island = DataManager.getIsland(islandId);
		// Ensure island and its zone exist
		if (island == null || island.getZone() == null) return;

		String disasterKey = islandId + ":" + type;
		// Prevent duplicate active disasters of the same type on the same island
		if (activeDisasters.contains(disasterKey)) return;
		activeDisasters.add(disasterKey);

		// Broadcast message to all players
		server.getPlayerManager().broadcast(Text.literal("Disaster on " + islandId + ": " + type.name()).formatted(Formatting.RED), false);

		// Apply effects and action bar notifications to players on the island
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			if (island.getZone().contains(player.getBlockPos())) {
				ActionbarNotifier.send(player, "§cDisaster: " + type.name() + "!");
				applyDisasterEffect(player, type, server);
			}
		}

		// Schedule removal of the active disaster flag after a delay (5 seconds = 100 ticks)
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				activeDisasters.remove(disasterKey);
			}
		}, 100 * 50); // 100 ticks * 50ms per tick = 5000ms = 5 seconds
	}

	/**
	 * Applies the specific effect of a disaster to a player.
	 * Effects include damage, status effects (freezing, blindness, poison, levitation).
	 *
	 * @param player The player to apply the effect to.
	 * @param type   The type of disaster.
	 * @param server The Minecraft server instance (needed for damage sources).
	 */
	private static void applyDisasterEffect(ServerPlayerEntity player, DisasterType type, MinecraftServer server) {
		switch (type) {
			case METEOR:
				player.damage(server.getOverworld().getDamageSources().generic(), 8.0F);
				break;
			case BLIZZARD:
				player.setFrozenTicks(player.getFrozenTicks() + 200);
				break;
			case SANDSTORM:
				player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
						net.minecraft.entity.effect.StatusEffects.BLINDNESS, 200, 0));
				break;
			case SPORE:
				player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
						net.minecraft.entity.effect.StatusEffects.POISON, 100, 0));
				break;
			case CRYSTAL_STORM:
				player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
						net.minecraft.entity.effect.StatusEffects.LEVITATION, 60, 0));
				break;
		}
	}

	/**
	 * Called every server tick via the registered event listener.
	 * Checks if the configured disaster interval (from {@link ConfigManager}) has passed.
	 * If so, randomly selects an island and a compatible disaster type based on {@link #DEFAULTS},
	 * then calls {@link #triggerDisaster(MinecraftServer, String, DisasterType)}.
	 *
	 * @param server The Minecraft server instance.
	 */
	private static void onServerTick(MinecraftServer server) {
		ticks++;
		// Check if the configured interval has passed
		if (ticks < ConfigManager.getDisasterIntervalTicks()) {
			return;
		}
		ticks = 0;

		// Select a random island
		List<Island> islands = new ArrayList<>(DataManager.getIslands().values());
		if (islands.isEmpty()) return;
		Island island = islands.get(new Random().nextInt(islands.size()));

		// Determine possible disasters for the island type
		DisasterType[] possibleDisasters = DEFAULTS.getOrDefault(island.getType(), new DisasterType[0]);
		if (possibleDisasters.length == 0) return;

		// Select a random disaster from the possibilities
		DisasterType disaster = possibleDisasters[new Random().nextInt(possibleDisasters.length)];

		// Trigger the selected disaster
		triggerDisaster(server, island.getId(), disaster);
	}
}