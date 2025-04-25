package de.nofelix.stormboundisles.game;

import de.nofelix.stormboundisles.data.*;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides utility methods for debugging and visualizing game elements.
 */
public class DebugManager {
	private static final Map<UUID, List<BlockPos>> borderBlocks = new HashMap<>();

	/**
	 * Toggle visualization of zone borders for a player.
	 *
	 * @param player The player entity for whom the borders are toggled.
	 * @param on     Whether to enable or disable the visualization.
	 */
	public static void toggleVisualizeBorders(ServerPlayerEntity player, boolean on) {
		UUID uid = player.getUuid();
		ServerWorld world = player.getServerWorld();
		if (on) {
			// Remove old if exists
			toggleVisualizeBorders(player, false);
			// Find player team
			Team team = DataManager.teams.values().stream()
					.filter(t -> t.members.contains(uid))
					.findFirst().orElse(null);
			if (team == null || team.islandId == null) return;
			Island island = DataManager.islands.get(team.islandId);
			if (island == null || island.zone == null) return;
			List<BlockPos> positions = computeBorderPositions(island);
			borderBlocks.put(uid, positions);
			// Place blocks
			for (BlockPos pos : positions) {
				world.setBlockState(pos, Blocks.GLASS.getDefaultState());
			}
		} else {
			List<BlockPos> positions = borderBlocks.remove(uid);
			if (positions != null) {
				for (BlockPos pos : positions) {
					world.setBlockState(pos, Blocks.AIR.getDefaultState());
				}
			}
		}
	}

	/**
	 * Computes the border positions for a given island's zone.
	 *
	 * @param island The island whose zone borders are to be computed.
	 * @return A list of BlockPos representing the border positions.
	 */
	private static List<BlockPos> computeBorderPositions(Island island) {
		Zone zone = island.zone;
		// determine y level for visualization: use island.spawnY if set, else minY+1
		int y;
		if (island.spawnY >= 0) {
			y = island.spawnY;
		} else if (zone instanceof BoundingBox bb) {
			y = bb.min.getY() + 1;
		} else if (zone instanceof PolygonZone pz) {
			y = pz.getMinY() + 1;
		} else {
			y = 64; // default
		}
		List<BlockPos> list = new ArrayList<>();
		if (zone instanceof BoundingBox bb) {
			int minX = bb.min.getX();
			int maxX = bb.max.getX();
			int minZ = bb.min.getZ();
			int maxZ = bb.max.getZ();
			for (int x = minX; x <= maxX; x++) {
				list.add(new BlockPos(x, y, minZ));
				list.add(new BlockPos(x, y, maxZ));
			}
			for (int z = minZ; z <= maxZ; z++) {
				list.add(new BlockPos(minX, y, z));
				list.add(new BlockPos(maxX, y, z));
			}
		} else if (zone instanceof PolygonZone pz) {
			List<BlockPos> pts = pz.getPoints();
			for (int i = 0; i < pts.size(); i++) {
				BlockPos a = pts.get(i);
				BlockPos b = pts.get((i + 1) % pts.size());
				int dx = b.getX() - a.getX();
				int dz = b.getZ() - a.getZ();
				int steps = Math.max(Math.abs(dx), Math.abs(dz));
				for (int s = 0; s <= steps; s++) {
					double fx = a.getX() + dx * (s / (double) steps);
					double fz = a.getZ() + dz * (s / (double) steps);
					int ix = MathHelper.floor(fx);
					int iz = MathHelper.floor(fz);
					list.add(new BlockPos(ix, y, iz));
				}
			}
		}
		// remove duplicates
		return list.stream().distinct().collect(Collectors.toList());
	}
}
