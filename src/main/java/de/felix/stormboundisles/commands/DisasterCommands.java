package de.felix.stormboundisles.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Implements disaster-related commands.
 */
public class DisasterCommands {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
				literal("isles")
						.then(literal("triggerdisaster")
								.then(argument("team", StringArgumentType.word())
										.then(argument("disaster", StringArgumentType.word())
												.executes(ctx -> {
													String team = StringArgumentType.getString(ctx, "team");
													String disaster = StringArgumentType.getString(ctx, "disaster");
													ctx.getSource().sendFeedback(() -> Text.literal("Triggered disaster " + disaster + " for " + team), false);
													// TODO: Actually trigger disaster via DisasterManager
													return 1;
												})
										)
								)
						)
						.then(literal("stopdisaster")
								.then(argument("team", StringArgumentType.word())
										.executes(ctx -> {
											String team = StringArgumentType.getString(ctx, "team");
											ctx.getSource().sendFeedback(() -> Text.literal("Stopped disaster for team " + team), false);
											// TODO: Actually stop disaster via DisasterManager
											return 1;
										})
								)
						)
						.then(literal("listdisasters")
								.executes(ctx -> {
									ctx.getSource().sendFeedback(() -> Text.literal("Disasters listed."), false);
									// TODO: List all registered disasters
									return 1;
								})
						)
		);
	}
}