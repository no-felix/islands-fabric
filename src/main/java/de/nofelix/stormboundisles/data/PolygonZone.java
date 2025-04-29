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
     * using an enhanced ray casting algorithm with improved edge case handling.
     *
     * @param pos The position to check.
     * @return True if the position's X and Z coordinates are inside the polygon, false otherwise.
     */
    @Override
    public boolean containsHorizontal(BlockPos pos) {
        // Check if the point lies exactly on any edge of the polygon
        if (isOnPolygonEdge(pos)) {
            return true;
        }
        
        double x = pos.getX() + 0.5; // Use center of block for more consistent results
        double z = pos.getZ() + 0.5;
        boolean inside = false;
        int n = points.size();
        
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = points.get(i).getX() + 0.5;
            double zi = points.get(i).getZ() + 0.5;
            double xj = points.get(j).getX() + 0.5;
            double zj = points.get(j).getZ() + 0.5;
            
            // Check if ray crosses edge
            boolean intersect = ((zi > z) != (zj > z)) && // z is between zi and zj
                  (x < (xj - xi) * (z - zi) / (zj - zi) + xi);
            
            if (intersect) inside = !inside;
        }
        return inside;
    }
    
    /**
     * Checks if a point lies exactly on any edge of the polygon.
     * This helps with edge case detection for more consistent behavior.
     *
     * @param pos The position to check.
     * @return True if the position lies on any edge of the polygon, false otherwise.
     */
    private boolean isOnPolygonEdge(BlockPos pos) {
        double x = pos.getX() + 0.5;
        double z = pos.getZ() + 0.5;
        int n = points.size();
        
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = points.get(i).getX() + 0.5;
            double zi = points.get(i).getZ() + 0.5;
            double xj = points.get(j).getX() + 0.5;
            double zj = points.get(j).getZ() + 0.5;
            
            // Check if point lies on line segment using distance calculation
            if (distanceToLineSegmentSquared(x, z, xi, zi, xj, zj) < 0.01) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Calculates the squared distance from a point to a line segment.
     * Used for edge detection in the polygon.
     *
     * @param x Point x coordinate
     * @param z Point z coordinate
     * @param x1 Line segment start x
     * @param z1 Line segment start z
     * @param x2 Line segment end x
     * @param z2 Line segment end z
     * @return Squared distance from point to line segment
     */
    private double distanceToLineSegmentSquared(double x, double z, double x1, double z1, double x2, double z2) {
        double lineLength = (x2 - x1) * (x2 - x1) + (z2 - z1) * (z2 - z1);
        if (lineLength == 0.0) return (x - x1) * (x - x1) + (z - z1) * (z - z1);
        
        // Calculate projection
        double t = ((x - x1) * (x2 - x1) + (z - z1) * (z2 - z1)) / lineLength;
        t = Math.max(0, Math.min(1, t));
        
        double projectionX = x1 + t * (x2 - x1);
        double projectionZ = z1 + t * (z2 - z1);
        
        return (x - projectionX) * (x - projectionX) + (z - projectionZ) * (z - projectionZ);
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