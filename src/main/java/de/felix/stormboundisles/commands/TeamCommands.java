package de.felix.stormboundisles.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.felix.stormboundisles.teams.Team;
import de.felix.stormboundisles.teams.TeamManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Implements team-related commands.
 */
public class TeamCommands {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
				literal("isles")
						.then(literal("addteam")
								.then(argument("teamname", StringArgumentType.word())
										.then(argument("color", StringArgumentType.word())
												.then(argument("islandtype", StringArgumentType.word())
														.executes(ctx -> {
															String name = StringArgumentType.getString(ctx, "teamname");
															String color = StringArgumentType.getString(ctx, "color");
															String islandType = StringArgumentType.getString(ctx, "islandtype");
															Team team = new Team(name, color, islandType);
															TeamManager.getInstance().addTeam(team);
															ctx.getSource().sendFeedback(() -> Text.literal("Team " + name + " added."), false);
															return 1;
														})
												)
										)
								)
						)
						.then(literal("removeteam")
								.then(argument("teamname", StringArgumentType.word())
										.executes(ctx -> {
											String name = StringArgumentType.getString(ctx, "teamname");
											TeamManager.getInstance().removeTeam(name);
											ctx.getSource().sendFeedback(() -> Text.literal("Team " + name + " removed."), false);
											return 1;
										})
								)
						)
		);
	}
}