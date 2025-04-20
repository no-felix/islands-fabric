package de.felix.stormboundisles.teams;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton manager for all teams.
 */
public class TeamManager {
	private static volatile TeamManager instance;
	private final Map<String, Team> teams = new HashMap<>(); // teamName -> Team

	private TeamManager() {
	}

	public synchronized static TeamManager getInstance() {
		if (instance == null) instance = new TeamManager();
		return instance;
	}

	public void addTeam(Team team) {
		teams.put(team.getName(), team);
	}

	public void removeTeam(String teamName) {
		teams.remove(teamName);
	}

	public Team getTeam(String teamName) {
		return teams.get(teamName);
	}

	public Collection<Team> getAllTeams() {
		return teams.values();
	}

	public Team getTeamOfPlayer(String playerUUID) {
		for (Team team : teams.values()) {
			if (team.getPlayerUUIDs().contains(playerUUID)) {
				return team;
			}
		}
		return null;
	}
}
