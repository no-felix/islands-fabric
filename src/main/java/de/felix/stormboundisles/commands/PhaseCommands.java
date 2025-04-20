package de.felix.stormboundisles.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Implements phase management commands.
 */
public class PhaseCommands {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
				literal("isles")
						.then(literal("start")
								.executes(ctx -> {
									ctx.getSource().sendFeedback(() -> Text.literal("Game started."), false);
									// TODO: Start game
									return 1;
								})
						)
						.then(literal("setphase")
										.then(literal("grace").executes(ctx -> {
											ctx.getSource().sendFeedback(() -> Text.literal("Phase set to grace."), false);
											// TODO: Set phase to grace
											return 1;
										}))
										.then(literal("battle").executes(ctx -> {
											ctx.getSource().sendFeedback(() -> Text.literal("Phase set to battle."), false);
											// TODO: Set phase to battle
											return 1;
										}))
								// Add more phases as needed
						)
						.then(literal("border")
								.then(literal("on").executes(ctx -> {
									ctx.getSource().sendFeedback(() -> Text.literal("Border enabled."), false);
									// TODO: Enable border
									return 1;
								}))
								.then(literal("off").executes(ctx -> {
									ctx.getSource().sendFeedback(() -> Text.literal("Border disabled."), false);
									// TODO: Disable border
									return 1;
								}))
						)
						.then(literal("reset")
								.executes(ctx -> {
									ctx.getSource().sendFeedback(() -> Text.literal("Game reset."), false);
									// TODO: Reset game state
									return 1;
								})
						)
						.then(literal("override")
								.executes(ctx -> {
									ctx.getSource().sendFeedback(() -> Text.literal("Admin override enabled."), false);
									// TODO: Enable override
									return 1;
								})
						)
		);
	}
}