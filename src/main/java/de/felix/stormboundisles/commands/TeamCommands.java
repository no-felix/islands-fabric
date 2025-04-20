package de.felix.stormboundisles.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Registers team-related commands.
 */
public class TeamCommands {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		// /isles addteam <teamname> <colorcode> <islandtype>
		// /isles removeteam <teamname>
		// /isles join <teamname> [player]
		// TODO: Implement command logic using Brigadier API
	}
}