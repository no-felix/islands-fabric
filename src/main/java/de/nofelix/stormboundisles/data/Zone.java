package de.nofelix.stormboundisles.data;

import net.minecraft.util.math.BlockPos;

/**
 * Represents a geometric zone in the game world.
 * Defines methods to check if a position is contained within the zone.
 */
public interface Zone {
    /**
     * Checks if the given position is fully contained within this zone, considering X, Y, and Z axes.
     *
     * @param pos The BlockPos to check.
     * @return {@code true} if the position is inside the zone, {@code false} otherwise.
     */
    boolean contains(BlockPos pos);

    /**
     * Checks if the given position is contained within the horizontal boundaries (X and Z axes) of this zone,
     * ignoring the Y-axis.
     *
     * @param pos The BlockPos to check.
     * @return {@code true} if the position's X and Z coordinates are inside the zone, {@code false} otherwise.
     */
    boolean containsHorizontal(BlockPos pos);
}