package de.felix.stormboundisles.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Implements buff-related commands (e.g. for testing or admin).
 */
public class BuffCommands {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
				literal("isles")
						.then(literal("addbuff")
								.then(argument("team", StringArgumentType.word())
										.then(argument("type", StringArgumentType.word())
												.executes(ctx -> {
													String team = StringArgumentType.getString(ctx, "team");
													String type = StringArgumentType.getString(ctx, "type");
													ctx.getSource().sendFeedback(() -> Text.literal("Added buff " + type + " to " + team), false);
													// TODO: Add buff to team via BuffManager
													return 1;
												})
										)
								)
						)
						.then(literal("removebuff")
								.then(argument("team", StringArgumentType.word())
										.then(argument("type", StringArgumentType.word())
												.executes(ctx -> {
													String team = StringArgumentType.getString(ctx, "team");
													String type = StringArgumentType.getString(ctx, "type");
													ctx.getSource().sendFeedback(() -> Text.literal("Removed buff " + type + " from " + team), false);
													// TODO: Remove buff from team via BuffManager
													return 1;
												})
										)
								)
						)
						.then(literal("listbuffs")
								.executes(ctx -> {
									ctx.getSource().sendFeedback(() -> Text.literal("Buffs listed."), false);
									// TODO: List all buffs
									return 1;
								})
						)
		);
	}
}