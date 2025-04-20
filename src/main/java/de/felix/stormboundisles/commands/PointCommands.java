package de.felix.stormboundisles.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.felix.stormboundisles.points.PointManager;
import de.felix.stormboundisles.scoreboard.ScoreboardManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Implements point/scoreboard-related commands.
 */
public class PointCommands {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
				literal("isles")
						.then(literal("points")
								.then(argument("team", StringArgumentType.word())
										.then(literal("add")
												.then(argument("amount", IntegerArgumentType.integer(1))
														.executes(ctx -> {
															String team = StringArgumentType.getString(ctx, "team");
															int amt = IntegerArgumentType.getInteger(ctx, "amount");
															PointManager.getInstance().addPoints(team, amt);
															ScoreboardManager.getInstance().updateAll(ctx.getSource().getServer());
															ctx.getSource().sendFeedback(() -> Text.literal("Added " + amt + " points to " + team), false);
															return 1;
														})
												)
										)
										.then(literal("remove")
												.then(argument("amount", IntegerArgumentType.integer(1))
														.executes(ctx -> {
															String team = StringArgumentType.getString(ctx, "team");
															int amt = IntegerArgumentType.getInteger(ctx, "amount");
															PointManager.getInstance().removePoints(team, amt);
															ScoreboardManager.getInstance().updateAll(ctx.getSource().getServer());
															ctx.getSource().sendFeedback(() -> Text.literal("Removed " + amt + " points from " + team), false);
															return 1;
														})
												)
										)
										.then(literal("set")
												.then(argument("amount", IntegerArgumentType.integer(0))
														.executes(ctx -> {
															String team = StringArgumentType.getString(ctx, "team");
															int amt = IntegerArgumentType.getInteger(ctx, "amount");
															PointManager.getInstance().setPoints(team, amt);
															ScoreboardManager.getInstance().updateAll(ctx.getSource().getServer());
															ctx.getSource().sendFeedback(() -> Text.literal("Set " + team + " points to " + amt), false);
															return 1;
														})
												)
										)
								)
						)
						.then(literal("deathpenalty")
								.then(argument("amount", IntegerArgumentType.integer(0))
										.executes(ctx -> {
											int amt = IntegerArgumentType.getInteger(ctx, "amount");
											PointManager.getInstance().setDeathPenalty(amt);
											ctx.getSource().sendFeedback(() -> Text.literal("Death penalty set to " + amt + " points."), false);
											return 1;
										})
								)
						)
						.then(literal("scoreboard")
								.executes(ctx -> {
									ScoreboardManager.getInstance().updateAll(ctx.getSource().getServer());
									ctx.getSource().sendFeedback(() -> Text.literal("Scoreboard updated."), false);
									return 1;
								})
						)
		);
	}
}