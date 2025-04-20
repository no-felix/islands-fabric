package de.felix.stormboundisles.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Registers disaster-related commands.
 */
public class DisasterCommands {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		// /isles triggerdisaster <teamname> <disastername>
		// /isles stopdisaster <teamname>
		// /isles listdisasters
		// TODO: Implement command logic using Brigadier API
	}
}
