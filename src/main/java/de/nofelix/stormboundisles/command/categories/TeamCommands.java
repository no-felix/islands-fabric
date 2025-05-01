package de.nofelix.stormboundisles.command.categories;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.nofelix.stormboundisles.command.CommandCategory;
import de.nofelix.stormboundisles.command.util.CommandPermissions;
import de.nofelix.stormboundisles.command.util.CommandSuggestions;
import de.nofelix.stormboundisles.data.DataManager;
import de.nofelix.stormboundisles.data.Island;
import de.nofelix.stormboundisles.data.Team;
import de.nofelix.stormboundisles.game.ScoreboardManager;
import de.nofelix.stormboundisles.util.Constants;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles team management commands for the Stormbound Isles mod.
 * <p>
 * This class provides commands for managing teams, including player assignment,
 * removal from teams, and viewing team information. The commands are structured with
 * different permission levels - all players can view team info, but only moderators
 * can modify team membership.
 * <p>
 * All team commands update both the persistent data storage and the in-game scoreboards.
 */
public class TeamCommands implements CommandCategory {
    /**
     * Registers all team management commands with the root command.
     * 
     * @param rootCommand The root command to add these commands to
     */
    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> rootCommand) {
        // Team category
        LiteralArgumentBuilder<ServerCommandSource> teamCommand = 
                CommandManager.literal("team")
                .requires(CommandPermissions.requiresPermissionLevel(CommandPermissions.PLAYER_PERMISSION_LEVEL)); // Base category available to all
                
        // Team assign command (moderator only)
        registerTeamAssignCommand(teamCommand);
        
        // Team remove command (moderator only)
        registerTeamRemoveCommand(teamCommand);
                
        // Team info command (all players)
        registerTeamInfoCommand(teamCommand);
                
        // Add team category to root command
        rootCommand.then(teamCommand);
    }
    
    /**
     * Registers the team assign command.
     * <p>
     * This command allows moderators to assign players to specific teams.
     * The command first removes the player from any existing teams,
     * then adds them to the specified team, updating all necessary data.
     * Both the command executor and the target player receive feedback.
     * 
     * @param teamCommand The parent team command to add this subcommand to
     */
    private void registerTeamAssignCommand(LiteralArgumentBuilder<ServerCommandSource> teamCommand) {
        LiteralArgumentBuilder<ServerCommandSource> assignCommand = 
                CommandManager.literal("assign")
                .requires(CommandPermissions.requiresPermissionLevel(CommandPermissions.MODERATOR_PERMISSION_LEVEL));
                
        assignCommand.then(CommandManager.argument("teamName", StringArgumentType.word())
                .suggests(CommandSuggestions.TEAM_NAME_SUGGESTIONS)
                .then(CommandManager.argument("player", StringArgumentType.word())
                        .suggests(CommandSuggestions.PLAYER_SUGGESTIONS)
                        .executes(ctx -> {
                            String teamName = StringArgumentType.getString(ctx, "teamName");
                            Team team = DataManager.getTeam(teamName);
                            if (team == null) {
                                ctx.getSource().sendError(Constants.TEAM_NOT_FOUND);
                                return 0;
                            }
                            ServerPlayerEntity target = ctx.getSource().getServer()
                                    .getPlayerManager().getPlayer(StringArgumentType.getString(ctx, "player"));
                            if (target == null) {
                                ctx.getSource().sendError(Constants.PLAYER_NOT_FOUND);
                                return 0;
                            }
                            
                            // Remove player from all teams
                            for (Team t : DataManager.getTeams().values()) {
                                t.removeMember(target.getUuid());
                            }
                            
                            // Add player to specified team
                            team.addMember(target.getUuid());
                            DataManager.saveAll();
                            ScoreboardManager.updateAllTeams(ctx.getSource().getServer());
                            ctx.getSource().sendFeedback(() ->
                                    Text.literal("Assigned " + target.getName().getString() +
                                            " to team " + teamName)
                                            .formatted(Formatting.GREEN), false);
                            
                            // Notify the player
                            target.sendMessage(Text.literal("You have been assigned to team " + teamName)
                                    .formatted(Formatting.GREEN, Formatting.BOLD));
                            return 1;
                        })
                )
        );
        
        teamCommand.then(assignCommand);
    }
    
    /**
     * Registers the team remove command.
     * <p>
     * This command allows moderators to remove players from all teams.
     * It checks all teams to find and remove the player, then updates data storage
     * and scoreboards. Both the command executor and the target player receive feedback.
     * 
     * @param teamCommand The parent team command to add this subcommand to
     */
    private void registerTeamRemoveCommand(LiteralArgumentBuilder<ServerCommandSource> teamCommand) {
        LiteralArgumentBuilder<ServerCommandSource> removeCommand = 
                CommandManager.literal("remove")
                .requires(CommandPermissions.requiresPermissionLevel(CommandPermissions.MODERATOR_PERMISSION_LEVEL));
                
        removeCommand.then(CommandManager.argument("player", StringArgumentType.word())
                .suggests(CommandSuggestions.PLAYER_SUGGESTIONS)
                .executes(ctx -> {
                    ServerPlayerEntity target = ctx.getSource().getServer()
                            .getPlayerManager().getPlayer(StringArgumentType.getString(ctx, "player"));
                    if (target == null) {
                        ctx.getSource().sendError(Constants.PLAYER_NOT_FOUND);
                        return 0;
                    }
                    
                    // Keep track if player was on any team
                    boolean wasOnTeam = false;
                    
                    // Remove player from all teams
                    for (Team t : DataManager.getTeams().values()) {
                        if (t.getMembers().contains(target.getUuid())) {
                            wasOnTeam = true;
                            t.removeMember(target.getUuid());
                        }
                    }
                    
                    if (!wasOnTeam) {
                        ctx.getSource().sendFeedback(() ->
                                Text.literal(target.getName().getString() + " was not on any team.")
                                        .formatted(Formatting.YELLOW), false);
                        return 0;
                    }
                    
                    DataManager.saveAll();
                    ScoreboardManager.updateAllTeams(ctx.getSource().getServer());
                    ctx.getSource().sendFeedback(() ->
                            Text.literal("Removed " + target.getName().getString() +
                                    " from all teams.")
                                    .formatted(Formatting.GREEN), false);
                    
                    // Notify the player
                    target.sendMessage(Text.literal("You have been removed from your team")
                            .formatted(Formatting.YELLOW));
                    return 1;
                })
        );
        
        teamCommand.then(removeCommand);
    }
    
    /**
     * Registers the team info command.
     * <p>
     * This command allows any player to view detailed information about a team,
     * including its points, associated island, and member list with online status
     * indicators. This command is available to all permission levels.
     * 
     * @param teamCommand The parent team command to add this subcommand to
     */
    private void registerTeamInfoCommand(LiteralArgumentBuilder<ServerCommandSource> teamCommand) {
        teamCommand.then(CommandManager.literal("info")
                .then(CommandManager.argument("teamName", StringArgumentType.word())
                        .suggests(CommandSuggestions.TEAM_NAME_SUGGESTIONS)
                        .executes(ctx -> {
                            String teamName = StringArgumentType.getString(ctx, "teamName");
                            Team team = DataManager.getTeam(teamName);
                            if (team == null) {
                                ctx.getSource().sendError(Constants.TEAM_NOT_FOUND);
                                return 0;
                            }
                            
                            // Get island info if available
                            String islandInfo = "None";
                            if (team.getIslandId() != null) {
                                Island island = DataManager.getIsland(team.getIslandId());
                                if (island != null) {
                                    islandInfo = island.getId() + " (" + island.getType() + ")";
                                }
                            }
                            
                            StringBuilder sb = new StringBuilder("§6Team: §d" + teamName + "§r\n");
                            sb.append("§6Points: §b").append(team.getPoints()).append("§r\n");
                            sb.append("§6Island: §e").append(islandInfo).append("§r\n");
                            sb.append("§6Members: §r");
                            
                            List<String> members = team.getMembers().stream()
                                .map(uuid -> {
                                    ServerPlayerEntity player = ctx.getSource().getServer()
                                        .getPlayerManager().getPlayer(uuid);
                                    if (player != null) {
                                        return "§a" + player.getName().getString() + "§r"; // Online
                                    } else {
                                        return "§7" + uuid.toString().substring(0, 8) + "§r"; // Offline or UUID only
                                    }
                                })
                                .collect(Collectors.toList());
                            
                            if (members.isEmpty()) {
                                sb.append("§7None§r");
                            } else {
                                sb.append(String.join(", ", members));
                            }
                            
                            ctx.getSource().sendFeedback(() -> Text.literal(sb.toString()), false);
                            return 1;
                        })
                )
        );
    }
}