package de.felix.stormboundisles.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Implements zone-related commands.
 */
public class ZoneCommands {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
				literal("isles")
						.then(literal("setislandzone")
								.then(argument("teamname", StringArgumentType.word())
										.then(literal("start")
												.executes(ctx -> {
													String team = StringArgumentType.getString(ctx, "teamname");
													ctx.getSource().sendFeedback(() -> Text.literal("Selection started for team " + team), false);
													// TODO: Start selection mode for user
													return 1;
												})
										)
										.then(literal("end")
												.executes(ctx -> {
													String team = StringArgumentType.getString(ctx, "teamname");
													ctx.getSource().sendFeedback(() -> Text.literal("Selection ended for team " + team), false);
													// TODO: End selection mode and save zone
													return 1;
												})
										)
								)
						)
						.then(literal("clearislandzone")
								.then(argument("teamname", StringArgumentType.word())
										.executes(ctx -> {
											String team = StringArgumentType.getString(ctx, "teamname");
											ctx.getSource().sendFeedback(() -> Text.literal("Zone for team " + team + " cleared."), false);
											// TODO: Remove zone from manager
											return 1;
										})
								)
						)
						.then(literal("listzones")
								.executes(ctx -> {
									ctx.getSource().sendFeedback(() -> Text.literal("Zones listed."), false);
									// TODO: List all zones
									return 1;
								})
						)
		);
	}
}
