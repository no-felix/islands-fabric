package de.felix.stormboundisles.zones;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Command registration for island zones.
 */
public class ZoneCommands {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		// TODO: Implement command registration for zone management.
		// /isles setislandzone <team> start, /isles setislandzone <team> end, /isles clearislandzone <team>
		// Implementation: Start creates a new ZoneSelectionSession, each right click/command adds a point,
		// End transfers points to ZoneManager and saves as IslandZone.
	}
}