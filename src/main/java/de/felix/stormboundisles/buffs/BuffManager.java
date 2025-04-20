package de.felix.stormboundisles.buffs;

import java.util.*;

/**
 * Manages buffs for teams/island types.
 */
public class BuffManager {
	private static BuffManager instance;
	private final Map<String, List<Buff>> buffsByIslandType = new HashMap<>();

	private BuffManager() {}

	public static BuffManager getInstance() {
		if (instance == null) instance = new BuffManager();
		return instance;
	}

	public void registerBuff(String islandType, Buff buff) {
		buffsByIslandType.computeIfAbsent(islandType, k -> new ArrayList<>()).add(buff);
	}

	public List<Buff> getBuffsForIsland(String islandType) {
		return buffsByIslandType.getOrDefault(islandType, Collections.emptyList());
	}

	// TODO: Apply/remove buffs to players based on zone/island membership
}
