package de.nofelix.stormboundisles.data;

import net.minecraft.util.math.BlockPos;

/**
 * Represents an axis-aligned bounding box defined by two corner points.
 * Implements the {@link Zone} interface.
 *
 * @deprecated Will be removed soon. Use {@link PolygonZone} instead.
 */
@Deprecated
public class BoundingBox implements Zone {
	/** The minimum corner point of the bounding box. */
	public BlockPos min;
	/** The maximum corner point of the bounding box. */
	public BlockPos max;

	/**
	 * Constructs a new BoundingBox from two corner points.
	 * The minimum and maximum points are determined automatically.
	 *
	 * @param a The first corner point.
	 * @param b The second corner point.
	 */
	public BoundingBox(BlockPos a, BlockPos b) {
		this.min = new BlockPos(
				Math.min(a.getX(), b.getX()),
				Math.min(a.getY(), b.getY()),
				Math.min(a.getZ(), b.getZ())
		);
		this.max = new BlockPos(
				Math.max(a.getX(), b.getX()),
				Math.max(a.getY(), b.getY()),
				Math.max(a.getZ(), b.getZ())
		);
	}

	/**
	 * Checks if the given position is contained within this bounding box (inclusive).
	 *
	 * @param pos The position to check.
	 * @return True if the position is inside the bounding box, false otherwise.
	 */
	public boolean contains(BlockPos pos) {
		return pos.getX() >= min.getX() && pos.getX() <= max.getX()
				&& pos.getY() >= min.getY() && pos.getY() <= max.getY()
				&& pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
	}

	/**
	 * Checks if the given position is contained within the horizontal boundaries of this bounding box,
	 * ignoring the Y-axis.
	 *
	 * @param pos The position to check.
	 * @return True if the position's X and Z coordinates are inside the bounding box, false otherwise.
	 */
	public boolean containsHorizontal(BlockPos pos) {
		return pos.getX() >= min.getX() && pos.getX() <= max.getX()
				&& pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
	}
}
