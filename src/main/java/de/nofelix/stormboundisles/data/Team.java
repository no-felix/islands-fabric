package de.nofelix.stormboundisles.data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a team in the game.
 */
public class Team {
    /** The name of the team. */
    public String name;
    /** A set of UUIDs representing the members of this team. */
    public Set<UUID> members = new HashSet<>();
    /** The ID of the island assigned to this team. */
    public String islandId;
    /** The points accumulated by this team for scoring purposes. */
    public int points = 0;

    /**
     * Constructs a new Team with the given name.
     * @param name The name of the team.
     */
    public Team(String name) {
        this.name = name;
    }
}