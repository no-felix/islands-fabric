package de.nofelix.stormboundisles.data;

import java.util.Objects;

/**
 * Represents an island in the game world.
 */
public class Island {
	/** The unique identifier for this island. */
	private final String id;
	/** The type of this island (e.g., STARTING, NORMAL, BOSS). */
	private IslandType type;
	/** The geographical zone this island belongs to. */
	private Zone zone;
	/** The name of the team currently assigned to this island. Null if unassigned. */
	private String teamName;
	/** The X-coordinate of the custom spawn point for this island. */
	private int spawnX = 0;
	/** The Y-coordinate of the custom spawn point for this island. A value less than 0 indicates an undefined spawn point. */
	private int spawnY = -1;
	/** The Z-coordinate of the custom spawn point for this island. */
	private int spawnZ = 0;

	/**
	 * Constructs a new Island with the given ID and type.
	 *
	 * @param id   The unique identifier for the island.
	 * @param type The type of the island.
	 * @throws IllegalArgumentException if id is null or empty, or if type is null
	 */
	public Island(String id, IslandType type) {
		if (id == null || id.isEmpty()) {
			throw new IllegalArgumentException("Island ID cannot be null or empty");
		}
		if (type == null) {
			throw new IllegalArgumentException("Island type cannot be null");
		}
		this.id = id;
		this.type = type;
	}

	/**
	 * Gets the island's unique identifier.
	 *
	 * @return The island ID
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the island's type.
	 *
	 * @return The island type
	 */
	public IslandType getType() {
		return type;
	}

	/**
	 * Sets the island's type.
	 *
	 * @param type The new island type
	 * @throws IllegalArgumentException if type is null
	 */
	public void setType(IslandType type) {
		if (type == null) {
			throw new IllegalArgumentException("Island type cannot be null");
		}
		this.type = type;
	}

	/**
	 * Gets the island's geographical zone.
	 *
	 * @return The island's zone, may be null if not defined
	 */
	public Zone getZone() {
		return zone;
	}

	/**
	 * Sets the island's geographical zone.
	 *
	 * @param zone The new geographical zone for this island
	 */
	public void setZone(Zone zone) {
		this.zone = zone;
	}

	/**
	 * Gets the name of the team assigned to this island.
	 *
	 * @return The team name, or null if no team is assigned
	 */
	public String getTeamName() {
		return teamName;
	}

	/**
	 * Assigns a team to this island.
	 *
	 * @param teamName The name of the team to assign, or null to clear assignment
	 */
	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	/**
	 * Gets the X-coordinate of the custom spawn point.
	 *
	 * @return The spawn X-coordinate
	 */
	public int getSpawnX() {
		return spawnX;
	}

	/**
	 * Gets the Y-coordinate of the custom spawn point.
	 * A value less than 0 indicates an undefined spawn point.
	 *
	 * @return The spawn Y-coordinate
	 */
	public int getSpawnY() {
		return spawnY;
	}

	/**
	 * Gets the Z-coordinate of the custom spawn point.
	 *
	 * @return The spawn Z-coordinate
	 */
	public int getSpawnZ() {
		return spawnZ;
	}

	/**
	 * Sets the custom spawn point coordinates for this island.
	 *
	 * @param x The X-coordinate
	 * @param y The Y-coordinate
	 * @param z The Z-coordinate
	 */
	public void setSpawnPoint(int x, int y, int z) {
		this.spawnX = x;
		this.spawnY = y;
		this.spawnZ = z;
	}

	/**
	 * Checks if this island has a defined spawn point.
	 *
	 * @return true if a spawn point is defined (Y >= 0), false otherwise
	 */
	public boolean hasSpawnPoint() {
		return spawnY >= 0;
	}

	/**
	 * Checks if this island has a defined zone.
	 *
	 * @return true if a zone is defined, false otherwise
	 */
	public boolean hasZone() {
		return zone != null;
	}

	/**
	 * Checks if this island has a team assigned to it.
	 *
	 * @return true if a team is assigned, false otherwise
	 */
	public boolean hasTeam() {
		return teamName != null && !teamName.isEmpty();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Island island = (Island) o;
		return Objects.equals(id, island.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "Island{" +
				"id='" + id + '\'' +
				", type=" + type +
				", hasZone=" + (zone != null) +
				", team='" + teamName + '\'' +
				", hasSpawn=" + (spawnY >= 0) +
				'}';
	}
}