package de.felix.stormboundisles.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Registers point/scoreboard-related commands.
 */
public class PointCommands {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		// /isles points <teamname> [add|remove|set] <amount>
		// /isles death <player>
		// /isles scoreboard
		// TODO: Implement command logic using Brigadier API
	}
}
