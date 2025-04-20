package de.felix.stormboundisles.bonus;

import de.felix.stormboundisles.points.PointManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles jury/admin bonus points and awards.
 */
public class BonusManager {
	private static BonusManager instance;
	private final Map<String, Integer> bonusPointsByTeam = new HashMap<>();

	private BonusManager() {
	}

	public static BonusManager getInstance() {
		if (instance == null) instance = new BonusManager();
		return instance;
	}

	public void addBonus(String team, int points) {
		bonusPointsByTeam.put(team, getBonus(team) + points);
	}

	public void setBonus(String team, int points) {
		bonusPointsByTeam.put(team, points);
	}

	public int getBonus(String team) {
		return bonusPointsByTeam.getOrDefault(team, 0);
	}

	public int getTotalPoints(String team) {
		return getBonus(team) + PointManager.getInstance().getPoints(team);
	}
}