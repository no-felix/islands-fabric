package de.felix.stormboundisles.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Registers all mod commands.
 */
public class CommandRegistry {
	public static void registerAll(CommandDispatcher<ServerCommandSource> dispatcher) {
		// TODO: Register all command classes here (zone, team, disaster, points, etc.)
		// ZoneCommands.register(dispatcher);
		// TeamCommands.register(dispatcher);
		// DisasterCommands.register(dispatcher);
		// PointCommands.register(dispatcher);
	}
}
