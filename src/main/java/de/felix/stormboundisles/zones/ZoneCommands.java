package de.felix.stormboundisles.zones;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Command registration for island zones.
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
													ServerPlayerEntity player = ctx.getSource().getPlayer();
													ZoneSelectionSession.startFor(player, team);
													ctx.getSource().sendFeedback(() -> Text.literal("Zone selection started for team " + team + ". Rechtsklick auf Eckpunkte!"), false);
													return 1;
												})
										)
										.then(literal("end")
												.executes(ctx -> {
													ServerPlayerEntity player = ctx.getSource().getPlayer();
													ZoneSelectionSession.finish(player);
													ctx.getSource().sendFeedback(() -> Text.literal("Zone selection ended."), false);
													return 1;
												})
										)
								)
						)
		);
	}
}