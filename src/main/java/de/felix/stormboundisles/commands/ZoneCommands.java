package de.felix.stormboundisles.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Registers island zone-related commands.
 */
public class ZoneCommands {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		// /isles setislandzone <teamname> start
		// /isles setislandzone <teamname> end
		// /isles clearislandzone <teamname>
		// /isles listzones
		// TODO: Implement command logic using Brigadier API
	}
}
