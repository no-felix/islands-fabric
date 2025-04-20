package de.felix.stormboundisles.points;

import de.felix.stormboundisles.teams.Team;
import de.felix.stormboundisles.teams.TeamManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages points for each team, applies penalties, and updates scoreboard.
 */
public class PointManager {
	private static PointManager instance;
	private final Map<String, Integer> pointsByTeam = new HashMap<>();
	private int deathPenalty = 1; // Default: -1 point per death, configurable

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
		updateScoreboard();
	}

	public void setPoints(String teamName, int value) {
		pointsByTeam.put(teamName, value);
		updateScoreboard();
	}

	public void removePoints(String teamName, int amount) {
		pointsByTeam.put(teamName, Math.max(0, getPoints(teamName) - amount));
		updateScoreboard();
	}

	/**
	 * Applies death penalty to the player's team.
	 */
	public void applyDeathPenalty(ServerPlayerEntity player) {
		Team team = TeamManager.getInstance().getTeamOfPlayer(player.getUuidAsString());
		if (team != null) {
			removePoints(team.getName(), deathPenalty);
		}
	}

	/**
	 * Sets the penalty applied on death.
	 */
	public void setDeathPenalty(int amount) {
		this.deathPenalty = amount;
	}

	/**
	 * Updates the in-game scoreboard for all players.
	 * (Stub: Actual implementation depends on Minecraft API)
	 */
	public void updateScoreboard() {
		// TODO: Implement scoreboard update logic using Minecraft API
		// e.g. update sidebar for all players with team scores
	}

	// Serialization/deserialization methods for config persistence can be added here
}