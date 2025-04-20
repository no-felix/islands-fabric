package de.felix.stormboundisles.disasters;

import java.util.*;

/**
 * Manages disasters for all teams/islands.
 */
public class DisasterManager {
	private static DisasterManager instance;
	private final Map<String, List<Disaster>> disastersByIslandType = new HashMap<>();
	private final Map<String, ActiveDisaster> activeDisasters = new HashMap<>(); // teamName -> active

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

	public boolean isDisasterActive(String teamName) {
		return activeDisasters.containsKey(teamName);
	}

	public void startDisaster(String teamName, Disaster disaster) {
		activeDisasters.put(teamName, new ActiveDisaster(disaster, System.currentTimeMillis()));
		// TODO: Trigger disaster effects (e.g., spawn, apply, schedule end)
	}

	public void endDisaster(String teamName) {
		activeDisasters.remove(teamName);
		// TODO: Cleanup disaster effects
	}

	public void tick() {
		// Called regularly to check if disasters should be triggered or ended
		// TODO: Implement trigger logic, random chances, and ending disasters after duration
	}

	public record ActiveDisaster(Disaster disaster, long startTimestamp) {}
}