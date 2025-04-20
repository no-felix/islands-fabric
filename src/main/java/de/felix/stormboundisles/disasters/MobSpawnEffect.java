package de.felix.stormboundisles.disasters;

import de.felix.stormboundisles.zones.IslandZone;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Random;

/**
 * Effect: Spawns mobs in the zone.
 */
public class MobSpawnEffect implements DisasterEffect {
	private final EntityType<?> mobType;
	private final int count;

	public MobSpawnEffect(EntityType<?> mobType, int count) {
		this.mobType = mobType;
		this.count = count;
	}

	@Override
	public void apply(ServerWorld world, IslandZone zone, List<ServerPlayerEntity> players) {
		Random rand = new Random();
		List<BlockPos> polygon = zone.getPolygonPoints();
		if (polygon.isEmpty()) return;

		int minX = polygon.stream().mapToInt(BlockPos::getX).min().orElse(0);
		int maxX = polygon.stream().mapToInt(BlockPos::getX).max().orElse(0);
		int minZ = polygon.stream().mapToInt(BlockPos::getZ).min().orElse(0);
		int maxZ = polygon.stream().mapToInt(BlockPos::getZ).max().orElse(0);

		for (int i = 0; i < count; i++) {
			int tries = 0;
			while (tries < 10) {
				int x = minX + rand.nextInt(maxX - minX + 1);
				int z = minZ + rand.nextInt(maxZ - minZ + 1);
				BlockPos pos = new BlockPos(x, world.getTopY(), z);
				if (zone.isInside(pos)) {
					MobEntity mob = (MobEntity) mobType.create(world);
					if (mob != null) {
						mob.refreshPositionAndAngles(x + 0.5, pos.getY(), z + 0.5, rand.nextFloat() * 360, 0);
						world.spawnEntity(mob);
					}
					break;
				}
				tries++;
			}
		}
	}

	@Override
	public void cleanup(ServerWorld world, IslandZone zone, List<ServerPlayerEntity> players) {
		// No cleanup needed for simple mob spawns
	}
}
