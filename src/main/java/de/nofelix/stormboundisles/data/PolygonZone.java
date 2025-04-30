package de.nofelix.stormboundisles.data;

import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a polygonal zone defined by a list of BlockPos points.
 * Allows horizontal containment via ray-casting and vertical range based on point Y values.
 */
public class PolygonZone implements Zone {
    /** The list of vertices defining the polygon in the horizontal plane (X and Z coordinates). */
    private final List<BlockPos> points;
    /** The minimum Y-coordinate among all vertices, defining the bottom of the zone. */
    private final int minY;
    /** The maximum Y-coordinate among all vertices, defining the top of the zone. */
    private final int maxY;
    /** Tolerance threshold for edge detection, determines how close a point must be to be considered on an edge */
    private static final double EDGE_TOLERANCE = 0.01;
    /** Offset to the center of a block from its corner coordinates */
    private static final double BLOCK_CENTER_OFFSET = 0.5;

    /**
     * Constructs a new PolygonZone from a list of vertices.
     * Calculates the minimum and maximum Y coordinates based on the provided points.
     *
     * @param points The list of BlockPos vertices defining the polygon.
     */
    public PolygonZone(List<BlockPos> points) {
        if (points == null || points.size() < 3) {
            throw new IllegalArgumentException("A polygon zone requires at least 3 points");
        }
        this.points = new ArrayList<>(points); // Create a defensive copy
        
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
     * Creates a rectangular zone from two corner points.
     * Computes the minimum and maximum X and Z coordinates to create a properly oriented rectangle
     * regardless of which corners are provided.
     *
     * @param corner1 The first corner position
     * @param corner2 The second corner position
     * @return A new PolygonZone representing the rectangle
     */
    public static PolygonZone createRectangle(BlockPos corner1, BlockPos corner2) {
        if (corner1 == null || corner2 == null) {
            throw new IllegalArgumentException("Corner positions cannot be null");
        }
        
        // Compute min and max coordinates
        int minX = Math.min(corner1.getX(), corner2.getX());
        int maxX = Math.max(corner1.getX(), corner2.getX());
        int minZ = Math.min(corner1.getZ(), corner2.getZ());
        int maxZ = Math.max(corner1.getZ(), corner2.getZ());
        int y = corner1.getY(); // Use the Y value from the first corner for all points
        
        // Create corner points in clockwise order
        List<BlockPos> rectanglePoints = new ArrayList<>(4);
        rectanglePoints.add(new BlockPos(minX, y, minZ)); // Top-left
        rectanglePoints.add(new BlockPos(maxX, y, minZ)); // Top-right
        rectanglePoints.add(new BlockPos(maxX, y, maxZ)); // Bottom-right
        rectanglePoints.add(new BlockPos(minX, y, maxZ)); // Bottom-left
        
        return new PolygonZone(rectanglePoints);
    }
    
    /**
     * Gets the list of vertices defining this polygon.
     *
     * @return An unmodifiable list of BlockPos vertices.
     */
    public List<BlockPos> getPoints() {
        return Collections.unmodifiableList(points);
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
     * Gets the X coordinate of the center of a block.
     * @param pos The BlockPos
     * @return The X coordinate of the block center
     */
    private double centerX(BlockPos pos) {
        return pos.getX() + BLOCK_CENTER_OFFSET;
    }

    /**
     * Gets the Z coordinate of the center of a block.
     * @param pos The BlockPos
     * @return The Z coordinate of the block center
     */
    private double centerZ(BlockPos pos) {
        return pos.getZ() + BLOCK_CENTER_OFFSET;
    }

    /**
     * Checks if a point lies exactly on any edge of the polygon.
     * This helps with edge case detection for more consistent behavior.
     *
     * @param pos The position to check.
     * @return True if the position lies on any edge of the polygon, false otherwise.
     */
    private boolean isOnPolygonEdge(BlockPos pos) {
        double x = centerX(pos);
        double z = centerZ(pos);
        int n = points.size();
        
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = centerX(points.get(i));
            double zi = centerZ(points.get(i));
            double xj = centerX(points.get(j));
            double zj = centerZ(points.get(j));
            
            // Check if point lies on line segment using distance calculation
            if (distanceToLineSegmentSquared(x, z, xi, zi, xj, zj) < EDGE_TOLERANCE) {
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
     * Checks if the given position is contained within the horizontal boundaries (X and Z) of this polygon
     * using an enhanced ray casting algorithm with improved edge case handling.
     * This method ignores the Y-coordinate, effectively treating the zone as infinitely tall.
     *
     * @param pos The position to check.
     * @return True if the position is inside the zone, false otherwise.
     */
    @Override
    public boolean contains(BlockPos pos) {
        // Check if the point lies exactly on any edge of the polygon
        if (isOnPolygonEdge(pos)) {
            return true;
        }

        double x = centerX(pos);
        double z = centerZ(pos);
        boolean inside = false;
        int n = points.size();

        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = centerX(points.get(i));
            double zi = centerZ(points.get(i));
            double xj = centerX(points.get(j));
            double zj = centerZ(points.get(j));

            // Check if ray crosses edge
            boolean intersect = ((zi > z) != (zj > z)) && // z is between zi and zj
                    (x < (xj - xi) * (z - zi) / (zj - zi) + xi);

            if (intersect) inside = !inside;
        }
        return inside;
    }
}