package de.nofelix.stormboundisles.data;

import net.minecraft.util.math.BlockPos;
import java.util.List;

/**
 * Represents a polygonal zone defined by a list of BlockPos points.
 * Allows horizontal containment via ray-casting and vertical range based on point Y values.
 */
public class PolygonZone implements Zone {
    /** The list of vertices defining the polygon in the horizontal plane (X and Z coordinates). */
    public final List<BlockPos> points;
    /** The minimum Y-coordinate among all vertices, defining the bottom of the zone. */
    public final int minY;
    /** The maximum Y-coordinate among all vertices, defining the top of the zone. */
    public final int maxY;

    /**
     * Constructs a new PolygonZone from a list of vertices.
     * Calculates the minimum and maximum Y coordinates based on the provided points.
     *
     * @param points The list of BlockPos vertices defining the polygon.
     */
    public PolygonZone(List<BlockPos> points) {
        this.points = points;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (BlockPos p : points) {
            min = Math.min(min, p.getY());
            max = Math.max(max, p.getY());
        }
        this.minY = min;
        this.maxY = max;
    }

    /**
     * Gets the list of vertices defining this polygon.
     *
     * @return The list of BlockPos vertices.
     */
    public List<BlockPos> getPoints() {
        return points;
    }

    /**
     * Gets the minimum Y-coordinate of this zone.
     *
     * @return The minimum Y-coordinate.
     */
    public int getMinY() {
        return minY;
    }

    /**
     * Gets the maximum Y-coordinate of this zone.
     *
     * @return The maximum Y-coordinate.
     */
    public int getMaxY() {
        return maxY;
    }

    /**
     * Checks if the given position is contained within the horizontal boundaries (X and Z) of this polygon
     * using the ray casting algorithm.
     *
     * @param pos The position to check.
     * @return True if the position's X and Z coordinates are inside the polygon, false otherwise.
     */
    @Override
    public boolean containsHorizontal(BlockPos pos) {
        double x = pos.getX();
        double z = pos.getZ();
        boolean inside = false;
        int n = points.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = points.get(i).getX();
            double zi = points.get(i).getZ();
            double xj = points.get(j).getX();
            double zj = points.get(j).getZ();
            boolean intersect = ((zi > z) != (zj > z)) &&
                (x < (xj - xi) * (z - zi) / (zj - zi) + xi);
            if (intersect) inside = !inside;
        }
        return inside;
    }

    /**
     * Checks if the given position is contained within this polygonal zone, considering both horizontal
     * position (using {@link #containsHorizontal(BlockPos)}) and vertical position (between minY and maxY, inclusive).
     *
     * @param pos The position to check.
     * @return True if the position is inside the zone, false otherwise.
     */
    @Override
    public boolean contains(BlockPos pos) {
        return pos.getY() >= minY && pos.getY() <= maxY && containsHorizontal(pos);
    }
}