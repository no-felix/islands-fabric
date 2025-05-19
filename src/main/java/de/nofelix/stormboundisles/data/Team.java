package de.nofelix.stormboundisles.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a team in the game.
 */
public class Team {
    /** The name of the team. */
    private final String name;
    /** A set of UUIDs representing the members of this team. */
    private final Set<UUID> members = new HashSet<>();
    /** The ID of the island assigned to this team. */
    private String islandId;
    /** The points accumulated by this team for scoring purposes. */
    private int points = 0;

    /**
     * Constructs a new Team with the given name.
     * 
     * @param name The name of the team.
     * @throws IllegalArgumentException if name is null or empty
     */
    public Team(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Team name cannot be null or empty");
        }
        this.name = name;
    }

    /**
     * Gets the name of the team.
     * 
     * @return The team name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets an unmodifiable view of the team members.
     * 
     * @return An unmodifiable set of member UUIDs
     */
    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    /**
     * Adds a player to the team.
     * 
     * @param playerUuid The UUID of the player to add
     * @return true if the player was added, false if they were already a member
     * @throws IllegalArgumentException if playerUuid is null
     */
    public boolean addMember(UUID playerUuid) {
        if (playerUuid == null) {
            throw new IllegalArgumentException("Player UUID cannot be null");
        }
        return members.add(playerUuid);
    }

    /**
     * Removes a player from the team.
     * 
     * @param playerUuid The UUID of the player to remove
     * @return true if the player was removed, false if they were not a member
     * @throws IllegalArgumentException if playerUuid is null
     */
    public boolean removeMember(UUID playerUuid) {
        if (playerUuid == null) {
            throw new IllegalArgumentException("Player UUID cannot be null");
        }
        return members.remove(playerUuid);
    }

    /**
     * Checks if a player is a member of this team.
     * 
     * @param playerUuid The UUID of the player to check
     * @return true if the player is a member, false otherwise
     * @throws IllegalArgumentException if playerUuid is null
     */
    public boolean isMember(UUID playerUuid) {
        if (playerUuid == null) {
            throw new IllegalArgumentException("Player UUID cannot be null");
        }
        return members.contains(playerUuid);
    }

    /**
     * Gets the ID of the island assigned to this team.
     * 
     * @return The island ID, or null if no island is assigned
     */
    public String getIslandId() {
        return islandId;
    }

    /**
     * Sets the ID of the island assigned to this team.
     * 
     * @param islandId The island ID to assign, or null to clear assignment
     */
    public void setIslandId(String islandId) {
        this.islandId = islandId;
    }

    /**
     * Gets the points accumulated by this team.
     * 
     * @return The team's points
     */
    public int getPoints() {
        return points;
    }

    /**
     * Sets the points for this team.
     * 
     * @param points The new point value (must be non-negative)
     * @throws IllegalArgumentException if points is negative
     */
    public void setPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points cannot be negative");
        }
        this.points = points;
    }

    /**
     * Adds points to this team's score.
     * 
     * @param pointsToAdd The number of points to add (can be negative to subtract)
     * @return The new points total
     */
    public int addPoints(int pointsToAdd) {
        int newPoints = Math.max(0, this.points + pointsToAdd);
        this.points = newPoints;
        return this.points;
    }

    /**
     * Checks if this team has an island assigned to it.
     * 
     * @return true if an island is assigned, false otherwise
     */
    public boolean hasIsland() {
        return islandId != null && !islandId.isEmpty();
    }

    /**
     * Gets the number of players in this team.
     * 
     * @return The team size
     */
    public int getSize() {
        return members.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Team team = (Team) o;
        return Objects.equals(name, team.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Team{" +
                "name='" + name + '\'' +
                ", members=" + members.size() +
                ", islandId='" + islandId + '\'' +
                ", points=" + points +
                '}';
    }
}