package de.nofelix.stormboundisles.data;

/**
 * Represents an island in the game world.
 */
public class Island {
	/** The unique identifier for this island. */
	public String id;
	/** The type of this island (e.g., STARTING, NORMAL, BOSS). */
	public IslandType type;
	/** The geographical zone this island belongs to. */
	public Zone zone;
	/** The name of the team currently assigned to this island. Null if unassigned. */
	public String teamName;
	/** The X-coordinate of the custom spawn point for this island. */
	public int spawnX = 0;
	/** The Y-coordinate of the custom spawn point for this island. A value less than 0 indicates an undefined spawn point. */
	public int spawnY = -1;
	/** The Z-coordinate of the custom spawn point for this island. */
	public int spawnZ = 0;

	/**
	 * Constructs a new Island with the given ID and type.
	 *
	 * @param id   The unique identifier for the island.
	 * @param type The type of the island.
	 */
	public Island(String id, IslandType type) {
		this.id = id;
		this.type = type;
	}
}