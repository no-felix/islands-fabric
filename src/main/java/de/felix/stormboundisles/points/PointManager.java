package de.felix.stormboundisles.points;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages points for each team.
 */
public class PointManager {
	private static PointManager instance;
	private final Map<String, Integer> pointsByTeam = new HashMap<>();

	private PointManager() {}

	public static PointManager getInstance() {
		if (instance == null) instance = new PointManager();
		return instance;
	}

	public int getPoints(String teamName) {
		return pointsByTeam.getOrDefault(teamName, 0);
	}

	public void addPoints(String teamName, int amount) {
		pointsByTeam.put(teamName, getPoints(teamName) + amount);
	}

	public void setPoints(String teamName, int value) {
		pointsByTeam.put(teamName, value);
	}

	public void removePoints(String teamName, int amount) {
		pointsByTeam.put(teamName, Math.max(0, getPoints(teamName) - amount));
	}

	// TODO: Death penalty, admin awards, scoreboard update, serialization
}