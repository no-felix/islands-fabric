package de.felix.stormboundisles.zones;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton manager for all island zones.
 */
public class ZoneManager {
	private static volatile ZoneManager instance;
	private final Map<String, IslandZone> zones = new HashMap<>();

	private ZoneManager() {}

	public synchronized static ZoneManager getInstance() {
		if (instance == null) instance = new ZoneManager();
		return instance;
	}

	public void addZone(IslandZone zone) {
		zones.put(zone.getTeamName(), zone);
	}

	public void removeZone(String teamName) {
		zones.remove(teamName);
	}

	public IslandZone getZoneForTeam(String teamName) {
		return zones.get(teamName);
	}

	public IslandZone getZoneForPlayer(ServerPlayerEntity player) {
		// Assumption: teamName via Player-Data/TeamManager.
		String teamName = ...; // TODO: Implementation depends on team handling
		return getZoneForTeam(teamName);
	}

	public IslandZone getZoneAt(BlockPos pos) {
		for (IslandZone zone : zones.values()) {
			if (zone.isInside(pos)) return zone;
		}
		return null;
	}

	public Collection<IslandZone> getAllZones() {
		return zones.values();
	}

	// Optional: Serialization to/from JSON for config storage
}
