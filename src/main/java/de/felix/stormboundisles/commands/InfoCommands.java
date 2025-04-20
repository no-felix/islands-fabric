package de.felix.stormboundisles.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Registers info/help-related commands.
 */
public class InfoCommands {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		// /isles info
		// /isles reload
		// TODO: Implement command logic using Brigadier API
	}
}
