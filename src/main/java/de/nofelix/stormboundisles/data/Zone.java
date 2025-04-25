package de.nofelix.stormboundisles.data;

import net.minecraft.util.math.BlockPos;

/**
 * Represents a geometric zone in the game world.
 * Defines methods to check if a position is contained within the zone.
 */
public interface Zone {
    /**
     * Checks if the given position is fully contained within this zone (considering all axes).
     *
     * @param pos The position to check.
     * @return True if the position is inside the zone, false otherwise.
     */
    boolean contains(BlockPos pos);

    /**
     * Checks if the given position is contained within the horizontal boundaries of this zone,
     * ignoring the Y-axis.
     *
     * @param pos The position to check.
     * @return True if the position's X and Z coordinates are inside the zone, false otherwise.
     */
    boolean containsHorizontal(BlockPos pos);
}