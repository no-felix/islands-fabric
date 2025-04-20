package de.felix.stormboundisles.teams;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a team in the game.
 */
public class Team {
	private final String name;
	private final String colorCode;
	private final Set<String> playerUUIDs = new HashSet<>();
	private final String islandType;

	public Team(String name, String colorCode, String islandType) {
		this.name = name;
		this.colorCode = colorCode;
		this.islandType = islandType;
	}

	/**
	 * Adds a player to the team.
	 *
	 * @param player The player to add.
	 */
	public void addPlayer(ServerPlayerEntity player) {
		playerUUIDs.add(player.getUuidAsString());
	}

	/**
	 * Removes a player from the team.
	 *
	 * @param player The player to remove.
	 */
	public void removePlayer(ServerPlayerEntity player) {
		playerUUIDs.remove(player.getUuidAsString());
	}

	/**
	 * Checks if a player is part of the team.
	 *
	 * @param player The player to check.
	 * @return True if the player is part of the team, false otherwise.
	 */
	public boolean hasPlayer(ServerPlayerEntity player) {
		return playerUUIDs.contains(player.getUuidAsString());
	}

	public String getName() {
		return name;
	}

	public String getColorCode() {
		return colorCode;
	}

	public Set<String> getPlayerUUIDs() {
		return playerUUIDs;
	}

	public String getIslandType() {
		return islandType;
	}
}
