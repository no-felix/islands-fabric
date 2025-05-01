package de.nofelix.stormboundisles.command.categories;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.nofelix.stormboundisles.command.CommandCategory;
import de.nofelix.stormboundisles.command.util.CommandPermissions;
import de.nofelix.stormboundisles.command.util.CommandSuggestions;
import de.nofelix.stormboundisles.data.DataManager;
import de.nofelix.stormboundisles.data.Island;
import de.nofelix.stormboundisles.data.Team;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Handles player-focused commands (player permission level).
 */
public class PlayerCommands implements CommandCategory {
    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> rootCommand) {
        // Player category - available to everyone
        LiteralArgumentBuilder<ServerCommandSource> playerCommand = 
                CommandManager.literal("player");
                
        // Player info command base node - no permission check here
        LiteralArgumentBuilder<ServerCommandSource> infoCommand = CommandManager.literal("info");
        
        // Self-info execution (available to all)
        infoCommand.executes(ctx -> {
            // Info about the player running the command
            ServerPlayerEntity player = ctx.getSource().getPlayer();
            if (player == null) {
                ctx.getSource().sendError(Text.literal("This command must be run by a player.")
                        .formatted(Formatting.RED));
                return 0;
            }
            
            return displayPlayerInfo(ctx, player);
        });
                
        // Other player info (moderator only)
        infoCommand.then(CommandManager.argument("player", StringArgumentType.word())
                .requires(CommandPermissions.requiresPermissionLevel(CommandPermissions.MODERATOR_PERMISSION_LEVEL))
                .suggests(CommandSuggestions.PLAYER_SUGGESTIONS)
                .executes(ctx -> {
                    ServerPlayerEntity target = ctx.getSource().getServer()
                            .getPlayerManager().getPlayer(StringArgumentType.getString(ctx, "player"));
                    if (target == null) {
                        ctx.getSource().sendError(Text.literal("Player not found.")
                                .formatted(Formatting.RED));
                        return 0;
                    }
                    
                    return displayPlayerInfo(ctx, target);
                })
        );
        
        // Add info command to player category
        playerCommand.then(infoCommand);
                
        // Add player category to root command
        rootCommand.then(playerCommand);
    }
    
    /**
     * Display information about a player.
     * 
     * @param ctx The command context
     * @param player The player to display information about
     * @return Command success code
     */
    private int displayPlayerInfo(com.mojang.brigadier.context.CommandContext<ServerCommandSource> ctx, 
                               ServerPlayerEntity player) {
        StringBuilder sb = new StringBuilder("§6Player: §a" + player.getName().getString() + "§r\n");
        
        // Find player's team
        String teamInfo = "§7None§r";
        for (Team team : DataManager.getTeams().values()) {
            if (team.getMembers().contains(player.getUuid())) {
                teamInfo = "§d" + team.getName() + "§r";
                break;
            }
        }
        
        sb.append("§6Team: ").append(teamInfo).append("\n");
        sb.append("§6Position: §e").append(player.getBlockPos().getX())
                .append(", ").append(player.getBlockPos().getY())
                .append(", ").append(player.getBlockPos().getZ())
                .append("§r\n");
        
        // Check what island they're in
        String currentIsland = "§7None§r";
        for (Island island : DataManager.getIslands().values()) {
            if (island.getZone() != null && island.getZone().contains(player.getBlockPos())) {
                currentIsland = "§e" + island.getId() + "§r";
                break;
            }
        }
        sb.append("§6Current Island: ").append(currentIsland);
        
        ctx.getSource().sendFeedback(() -> Text.literal(sb.toString()), false);
        return 1;
    }
}