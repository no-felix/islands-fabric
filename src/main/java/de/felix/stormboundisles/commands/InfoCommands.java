package de.felix.stormboundisles.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Implements info/help-related commands.
 */
public class InfoCommands {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
				literal("isles")
						.then(literal("info")
								.executes(ctx -> {
									ctx.getSource().sendFeedback(() -> Text.literal("Stormbound Isles Mod - Usage: /isles <command>"), false);
									// TODO: Show more info or help
									return 1;
								})
						)
						.then(literal("reload")
								.executes(ctx -> {
									ctx.getSource().sendFeedback(() -> Text.literal("Config reloaded."), false);
									// TODO: Trigger config reload
									return 1;
								})
						)
		);
	}
}