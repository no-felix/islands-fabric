package de.nofelix.stormboundisles.command.util;

import de.nofelix.stormboundisles.data.PolygonZone;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages polygon building state for island zones.
 */
public class PolygonBuilderManager {
    /**
     * Represents a polygon being built by a player.
     */
    public static class PolygonBuilder {
        private final String islandId;
        private final List<BlockPos> points = new ArrayList<>();
        
        public PolygonBuilder(String islandId) {
            this.islandId = islandId;
        }
        
        public String getIslandId() {
            return islandId;
        }
        
        public List<BlockPos> getPoints() {
            return points;
        }
        
        public void addPoint(BlockPos point) {
            points.add(point);
        }
        
        public int getPointCount() {
            return points.size();
        }
        
        /**
         * Create a rectangle zone from the first two points.
         */
        public PolygonZone createRectangle(BlockPos secondPoint) {
            if (points.isEmpty()) {
                throw new IllegalStateException("No points defined for rectangle");
            }
            
            BlockPos firstPoint = points.get(0);
            return PolygonZone.createRectangle(firstPoint, secondPoint);
        }
        
        /**
         * Create a polygon zone from all points.
         */
        public PolygonZone createPolygon() {
            if (points.size() < 3) {
                throw new IllegalStateException("At least 3 points are required for a polygon");
            }
            
            return new PolygonZone(points);
        }
    }
    
    private static final Map<UUID, PolygonBuilder> polygonBuilders = new Object2ObjectOpenHashMap<>();
    
    /**
     * Start a new polygon building session for a player.
     * 
     * @param playerId The UUID of the player
     * @param islandId The ID of the island to build the polygon for
     * @return The new polygon builder
     */
    public static PolygonBuilder startPolygon(UUID playerId, String islandId) {
        PolygonBuilder builder = new PolygonBuilder(islandId);
        polygonBuilders.put(playerId, builder);
        return builder;
    }
    
    /**
     * Get the current polygon builder for a player.
     * 
     * @param playerId The UUID of the player
     * @return The polygon builder, or null if none exists
     */
    public static PolygonBuilder getBuilder(UUID playerId) {
        return polygonBuilders.get(playerId);
    }
    
    /**
     * Remove and return the polygon builder for a player.
     * 
     * @param playerId The UUID of the player
     * @return The removed polygon builder, or null if none existed
     */
    public static PolygonBuilder removeBuilder(UUID playerId) {
        return polygonBuilders.remove(playerId);
    }
}