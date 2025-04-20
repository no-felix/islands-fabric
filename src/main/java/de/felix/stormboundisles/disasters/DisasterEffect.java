package de.felix.stormboundisles.disasters;

import de.felix.stormboundisles.zones.IslandZone;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.List;

/**
 * Interface for effects that a disaster can cause.
 */
public interface DisasterEffect {
	/**
	 * Called when the disaster starts.
	 */
	void apply(ServerWorld world, IslandZone zone, List<ServerPlayerEntity> players);

	/**
	 * Called when the disaster ends, to clean up if necessary.
	 */
	void cleanup(ServerWorld world, IslandZone zone, List<ServerPlayerEntity> players);
}