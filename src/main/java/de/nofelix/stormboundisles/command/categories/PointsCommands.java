package de.nofelix.stormboundisles.command.categories;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.nofelix.stormboundisles.command.CommandCategory;
import de.nofelix.stormboundisles.command.util.CommandPermissions;
import de.nofelix.stormboundisles.command.util.CommandSuggestions;
import de.nofelix.stormboundisles.data.DataManager;
import de.nofelix.stormboundisles.data.Team;
import de.nofelix.stormboundisles.game.ScoreboardManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Handles points management commands.
 */
public class PointsCommands implements CommandCategory {
    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> rootCommand) {
        // Points category - now entirely moderator level
        LiteralArgumentBuilder<ServerCommandSource> pointsCommand = 
                CommandManager.literal("points")
                .requires(CommandPermissions.requiresPermissionLevel(CommandPermissions.MODERATOR_PERMISSION_LEVEL));
                
        // Points add command
        LiteralArgumentBuilder<ServerCommandSource> addCommand = CommandManager.literal("add");
                
        addCommand.then(CommandManager.argument("team", StringArgumentType.word())
                .suggests(CommandSuggestions.TEAM_NAME_SUGGESTIONS)
                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .then(CommandManager.argument("reason", StringArgumentType.greedyString())
                                .executes(ctx -> handlePointsChange(ctx, true, true))
                        )
                        .executes(ctx -> handlePointsChange(ctx, true, false))
                )
        );
                
        // Points remove command
        LiteralArgumentBuilder<ServerCommandSource> removeCommand = CommandManager.literal("remove");
                
        removeCommand.then(CommandManager.argument("team", StringArgumentType.word())
                .suggests(CommandSuggestions.TEAM_NAME_SUGGESTIONS)
                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .then(CommandManager.argument("reason", StringArgumentType.greedyString())
                                .executes(ctx -> handlePointsChange(ctx, false, true))
                        )
                        .executes(ctx -> handlePointsChange(ctx, false, false))
                )
        );
                
        // Add subcategories to points category
        pointsCommand.then(addCommand);
        pointsCommand.then(removeCommand);
                
        // Add points category to root command
        rootCommand.then(pointsCommand);
    }
    
    /**
     * Handle points addition or removal with consolidated logic.
     * 
     * @param ctx The command context
     * @param isAddition Whether points are being added (true) or removed (false)
     * @param hasReason Whether a reason was provided
     * @return Command success code
     */
    private int handlePointsChange(CommandContext<ServerCommandSource> ctx, 
                                 boolean isAddition, boolean hasReason) {
        Team team = DataManager.getTeam(StringArgumentType.getString(ctx, "team"));
        if (team == null) {
            ctx.getSource().sendError(Text.literal("Team does not exist.")
                    .formatted(Formatting.RED));
            return 0;
        }
        
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        int pointChange = isAddition ? amount : -amount; // Negate for removal
        
        team.addPoints(pointChange);
        DataManager.saveAll();
        ScoreboardManager.updateTeamScore(team.getName());
        
        // Build feedback message
        final String actionText = isAddition 
                ? "§aAdded §b" + amount + "§a points to team §d" + team.getName() + "§r" 
                : "§cRemoved §b" + amount + "§c points from team §d" + team.getName() + "§r";
        
        if (hasReason) {
            final String reason = StringArgumentType.getString(ctx, "reason");
            final String fullFeedback = actionText + " §7(" + reason + ")§r";
            
            // Broadcast point change to all players
            ctx.getSource().getServer().getPlayerManager().broadcast(
                    Text.literal(fullFeedback), false);
            return 1;
        }
        
        // Just inform the command executor
        ctx.getSource().sendFeedback(() -> Text.literal(actionText), false);
        return 1;
    }
}