package de.felix.stormboundisles.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Registers point/scoreboard-related commands.
 */
public class PointCommands {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		// Example command registration (Brigadier pseudo-code)
		// /isles points <team> add <amount>
		// /isles points <team> set <amount>
		// /isles points <team> remove <amount>
		// /isles deathpenalty <amount>
		// /isles scoreboard

		// TODO: Implement actual argument parsing and execution logic
		// Example for /isles points <team> add <amount>
		// dispatcher.register(
		//     literal("isles").then(literal("points")
		//         .then(argument("team", StringArgumentType.word())
		//             .then(literal("add")
		//                 .then(argument("amount", IntegerArgumentType.integer(1))
		//                     .executes(ctx -> {
		//                         String team = StringArgumentType.getString(ctx, "team");
		//                         int amt = IntegerArgumentType.getInteger(ctx, "amount");
		//                         PointManager.getInstance().addPoints(team, amt);
		//                         ctx.getSource().sendFeedback(Text.literal("Added " + amt + " points to " + team), false);
		//                         return 1;
		//                     })
		//                 )
		//             )
		//         )
		//     )
		// );
	}
}