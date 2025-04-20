package de.felix.stormboundisles.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Registers phase management commands.
 */
public class PhaseCommands {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		// /isles start
		// /isles setphase <phase>
		// /isles border <on|off>
		// /isles reset
		// /isles override
		// TODO: Implement command logic using Brigadier API
	}
}
