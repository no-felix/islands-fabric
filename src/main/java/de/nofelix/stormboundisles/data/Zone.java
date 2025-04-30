package de.nofelix.stormboundisles.data;

import net.minecraft.util.math.BlockPos;

/**
 * Represents a geometric zone in the game world.
 * Defines methods to check if a position is contained within the zone.
 */
public interface Zone {
    /**
     * Checks if the given position is contained within the horizontal boundaries (X and Z axes) of this zone.
     * Implementations should typically ignore the Y-axis, treating the zone as infinitely tall.
     *
     * @param pos The BlockPos to check.
     * @return {@code true} if the position's X and Z coordinates are inside the zone, {@code false} otherwise.
     */
    boolean contains(BlockPos pos);
}