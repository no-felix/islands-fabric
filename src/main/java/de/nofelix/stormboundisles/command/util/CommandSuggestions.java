package de.nofelix.stormboundisles.command.util;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.nofelix.stormboundisles.data.DataManager;
import de.nofelix.stormboundisles.disaster.DisasterType;
import de.nofelix.stormboundisles.game.GamePhase;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for command suggestions.
 */
public class CommandSuggestions {
    // Cached suggestion lists
    private static List<String> GAME_PHASE_NAMES;
    private static List<String> DISASTER_TYPE_NAMES;
    
    // Suggestion providers
    public static SuggestionProvider<ServerCommandSource> ISLAND_ID_SUGGESTIONS;
    public static SuggestionProvider<ServerCommandSource> TEAM_NAME_SUGGESTIONS;
    public static SuggestionProvider<ServerCommandSource> DISASTER_TYPE_SUGGESTIONS;
    public static SuggestionProvider<ServerCommandSource> GAME_PHASE_SUGGESTIONS;
    public static SuggestionProvider<ServerCommandSource> PLAYER_SUGGESTIONS;
    
    /**
     * Initialize all suggestion providers.
     * Must be called before using any suggestion provider.
     */
    public static void initialize() {
        // Cache enum values
        GAME_PHASE_NAMES = Stream.of(GamePhase.values())
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableList());
        
        DISASTER_TYPE_NAMES = Stream.of(DisasterType.values())
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableList());
        
        // Create suggestion providers
        ISLAND_ID_SUGGESTIONS = (ctx, builder) ->
                CommandSource.suggestMatching(DataManager.getIslands().keySet(), builder);
        
        TEAM_NAME_SUGGESTIONS = (ctx, builder) ->
                CommandSource.suggestMatching(DataManager.getTeams().keySet(), builder);
        
        DISASTER_TYPE_SUGGESTIONS = (ctx, builder) ->
                CommandSource.suggestMatching(DISASTER_TYPE_NAMES, builder);
        
        GAME_PHASE_SUGGESTIONS = (ctx, builder) ->
                CommandSource.suggestMatching(GAME_PHASE_NAMES, builder);
        
        PLAYER_SUGGESTIONS = (ctx, builder) ->
                CommandSource.suggestMatching(
                        ctx.getSource().getServer().getPlayerManager().getPlayerList()
                                .stream().map(p -> p.getName().getString())
                                .collect(Collectors.toList()), 
                        builder);
    }
}