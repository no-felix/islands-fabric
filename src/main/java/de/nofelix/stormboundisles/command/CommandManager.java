package de.nofelix.stormboundisles.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.nofelix.stormboundisles.command.categories.*;
import de.nofelix.stormboundisles.command.util.CommandSuggestions;
import de.nofelix.stormboundisles.data.DataManager;
import de.nofelix.stormboundisles.data.Island;
import de.nofelix.stormboundisles.data.IslandType;
import de.nofelix.stormboundisles.data.Team;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Main command manager for the mod.
 * Delegates commands to specialized command category handlers.
 */
public class CommandManager {
    // Command constants
    private static final String SBI = "sbi";
    
    // Command categories
    private final AdminCommands adminCommands;
    private final IslandCommands islandCommands;
    private final TeamCommands teamCommands;
    private final PointsCommands pointsCommands;
    private final PlayerCommands playerCommands;

    public CommandManager() {
        // Initialize the command suggestions helper
        CommandSuggestions.initialize();
        
        // Create command categories
        this.adminCommands = new AdminCommands();
        this.islandCommands = new IslandCommands();
        this.teamCommands = new TeamCommands();
        this.pointsCommands = new PointsCommands();
        this.playerCommands = new PlayerCommands();
        
        // Initialize default islands and teams
        initIslandsAndTeams();
    }

    /**
     * Register all commands with the game.
     */
    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Main command builder
            LiteralArgumentBuilder<ServerCommandSource> sbiCommand = 
                    net.minecraft.server.command.CommandManager.literal(SBI);
            
            // Register all command categories
            adminCommands.register(sbiCommand);
            islandCommands.register(sbiCommand);
            teamCommands.register(sbiCommand);
            pointsCommands.register(sbiCommand);
            playerCommands.register(sbiCommand);
            
            // Register the command
            dispatcher.register(sbiCommand);
        });
    }
    
    /**
     * Initialize all islands and teams, ensuring they exist and are properly linked.
     */
    private void initIslandsAndTeams() {
        for (IslandType type : IslandType.values()) {
            String id = type.name().toLowerCase();
            Island island = DataManager.getIsland(id);
            if (island == null) {
                island = new Island(id, type);
                DataManager.putIsland(island);
            }
            
            String teamName = type.name();
            Team team = DataManager.getTeam(teamName);
            if (team == null) {
                team = new Team(teamName);
                DataManager.putTeam(team);
            }

            if (island.getTeamName() == null) island.setTeamName(teamName);
            if (team.getIslandId() == null) team.setIslandId(id);
        }
        DataManager.saveAll();
    }
}