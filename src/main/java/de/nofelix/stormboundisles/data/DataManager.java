package de.nofelix.stormboundisles.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import de.nofelix.stormboundisles.StormboundIslesMod;
import de.nofelix.stormboundisles.game.GameManager;
import de.nofelix.stormboundisles.game.GamePhase;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Manages loading and saving persistent game data, including team information,
 * island definitions, and the current game state (phase and progress).
 * Data is stored in JSON files within the world save directory (`world/stormboundisles`).
 * This class uses static methods and fields for global access.
 */
public final class DataManager {
    // Constants
    private static final String DATA_DIR_NAME = "stormboundisles";
    private static final String ISLANDS_FILENAME = "islands.json";
    private static final String TEAMS_FILENAME = "teams.json";
    private static final String GAME_STATE_FILENAME = "game_state.json";

    // JSON serialization configuration
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    // Type tokens for JSON deserialization
    private static final Type TEAM_MAP_TYPE = new TypeToken<Map<String, Team>>() {}.getType();
    private static final Type ISLAND_MAP_TYPE = new TypeToken<Map<String, Island>>() {}.getType();

    // Data storage - Using ConcurrentHashMap for thread safety with static fields
    private static final Map<String, Team> teams = new ConcurrentHashMap<>();
    private static final Map<String, Island> islands = new ConcurrentHashMap<>();

    // Constructor - prevent instantiation of this utility class
    private DataManager() {
        throw new UnsupportedOperationException("DataManager is a utility class and cannot be instantiated.");
    }

    // --- Public API ---

    /**
     * Returns an unmodifiable view of the teams map.
     * Modifications should be done via {@link #putTeam(Team)}.
     *
     * @return An unmodifiable map of team names to Team objects.
     */
    @NotNull
    public static Map<String, Team> getTeams() {
        return Collections.unmodifiableMap(teams);
    }

    /**
     * Returns an unmodifiable view of the islands map.
     * Modifications should be done via {@link #putIsland(Island)}.
     *
     * @return An unmodifiable map of island IDs to Island objects.
     */
    @NotNull
    public static Map<String, Island> getIslands() {
        return Collections.unmodifiableMap(islands);
    }

    /**
     * Gets a team by its name.
     *
     * @param teamName The name of the team to retrieve. Must not be null.
     * @return The {@link Team} object, or {@code null} if no team exists with the given name.
     */
    @Nullable
    public static Team getTeam(@NotNull String teamName) {
        return teams.get(teamName);
    }

    /**
     * Gets an island by its ID.
     *
     * @param islandId The ID of the island to retrieve. Must not be null.
     * @return The {@link Island} object, or {@code null} if no island exists with the given ID.
     */
    @Nullable
    public static Island getIsland(@NotNull String islandId) {
        return islands.get(islandId);
    }

    /**
     * Adds or updates a team in the teams collection.
     * This operation is thread-safe.
     *
     * @param team The team to add or update. Must not be null, and its name must not be null.
     * @throws IllegalArgumentException If the team or team name is null.
     */
    public static void putTeam(@NotNull Team team) {
        if (team.getName() == null) {
            throw new IllegalArgumentException("Cannot add a team with a null name.");
        }
        teams.put(team.getName(), team);
    }

    /**
     * Adds or updates an island in the islands collection.
     * This operation is thread-safe.
     *
     * @param island The island to add or update. Must not be null, and its ID must not be null.
     * @throws IllegalArgumentException If the island or island ID is null.
     */
    public static void putIsland(@NotNull Island island) {
        if (island.getId() == null) {
            throw new IllegalArgumentException("Cannot add an island with a null ID.");
        }
        islands.put(island.getId(), island);
    }

    /**
     * Clears all teams from the in-memory collection.
     * Does not automatically persist this change; call {@link #saveAll()} or {@link #saveTeams()} if needed.
     */
    public static void clearTeams() {
        teams.clear();
        StormboundIslesMod.LOGGER.info("Cleared all in-memory teams.");
    }

    /**
     * Clears all islands from the in-memory collection.
     * Does not automatically persist this change; call {@link #saveAll()} or {@link #saveIslands()} if needed.
     */
    public static void clearIslands() {
        islands.clear();
        StormboundIslesMod.LOGGER.info("Cleared all in-memory islands.");
    }

    /**
     * Loads all data (islands, teams, game state) from the default directory structure.
     * Uses the game directory provided by FabricLoader.
     * This should typically be called during mod initialization.
     */
    public static void loadAll() {
        Path runDir = FabricLoader.getInstance().getGameDir();
        load(runDir);
    }

    /**
     * Loads all data (islands, teams, game state) from the appropriate JSON files
     * within the specified run directory structure.
     * If files don't exist or are invalid, attempts to proceed with default/empty data.
     *
     * @param runDir The base directory where the game is running (e.g., server root). Must not be null.
     */
    public static void load(@NotNull Path runDir) {
        StormboundIslesMod.LOGGER.info("Loading Stormbound Isles data from base directory: {}", runDir);
        try {
            Path dataDir = ensureDataDirectory(runDir);

            loadIslands(dataDir);
            loadTeams(dataDir);
            loadGameState(dataDir);

            StormboundIslesMod.LOGGER.info("Finished loading Stormbound Isles data.");

        } catch (IOException e) {
            StormboundIslesMod.LOGGER.error("Fatal error accessing data directory '{}'. Data loading aborted.", DATA_DIR_NAME, e);
        } catch (Exception e) {
            StormboundIslesMod.LOGGER.error("Unexpected error during data loading process.", e);
        }
    }

    /**
     * Saves all data (islands, teams, game state) to the default directory structure.
     * Uses the game directory provided by FabricLoader.
     * This might be called on server stop or periodically.
     */
    public static void saveAll() {
        Path runDir = FabricLoader.getInstance().getGameDir();
        saveAll(runDir);
    }

    /**
     * Saves all data (islands, teams, game state) to the appropriate JSON files
     * within the specified run directory structure.
     *
     * @param runDir The base directory where the game is running (e.g., server root). Must not be null.
     */
    public static void saveAll(@NotNull Path runDir) {
        StormboundIslesMod.LOGGER.info("Saving all Stormbound Isles data to base directory: {}", runDir);
        try {
            Path dataDir = ensureDataDirectory(runDir);

            saveIslands(dataDir);
            saveTeams(dataDir);
            saveGameState(dataDir);

            StormboundIslesMod.LOGGER.info("Finished saving Stormbound Isles data.");

        } catch (IOException e) {
            StormboundIslesMod.LOGGER.error("Fatal error accessing data directory '{}'. Data saving aborted.", DATA_DIR_NAME, e);
        } catch (Exception e) {
            StormboundIslesMod.LOGGER.error("Unexpected error during data saving process.", e);
        }
    }

    /**
     * Saves only the current game state (phase and ticks) to its JSON file
     * in the default directory structure.
     * Uses the game directory provided by FabricLoader.
     */
    public static void saveGameState() {
        Path runDir = FabricLoader.getInstance().getGameDir();
        try {
            Path dataDir = ensureDataDirectory(runDir);
            saveGameState(dataDir);
        } catch (IOException e) {
            StormboundIslesMod.LOGGER.error("Error accessing data directory for saving game state.", e);
        } catch (Exception e) {
            StormboundIslesMod.LOGGER.error("Unexpected error during game state saving.", e);
        }
    }

    // --- Private Implementation Methods ---

    /**
     * Creates and ensures the existence of the data directory for the mod
     * (e.g., `[runDir]/world/stormboundisles` or `[runDir]/stormboundisles`).
     *
     * @param runDir The base directory for the game (server root or client run dir). Must not be null.
     * @return The {@link Path} to the data directory.
     * @throws IOException If directory creation fails due to I/O errors.
     */
    @NotNull
    private static Path ensureDataDirectory(@NotNull Path runDir) throws IOException {
        Path worldDir = runDir.resolve("world");
        Path baseDir = Files.isDirectory(worldDir) ? worldDir : runDir;

        Path dataDir = baseDir.resolve(DATA_DIR_NAME);

        Files.createDirectories(dataDir);

        return dataDir;
    }

    /**
     * Loads islands data from the specified JSON file.
     * Clears current in-memory islands before loading. Handles file reading and JSON parsing errors.
     *
     * @param dataDir The data directory containing the islands file. Must not be null.
     */
    private static void loadIslands(@NotNull Path dataDir) {
        Path islandsPath = dataDir.resolve(ISLANDS_FILENAME);
        islands.clear();

        if (Files.exists(islandsPath)) {
            try (var reader = Files.newBufferedReader(islandsPath, StandardCharsets.UTF_8)) {
                Map<String, Island> loadedIslands = GSON.fromJson(reader, ISLAND_MAP_TYPE);
                if (loadedIslands != null) {
                    islands.putAll(loadedIslands);
                    StormboundIslesMod.LOGGER.info("Loaded {} islands from {}", islands.size(), islandsPath.getFileName());
                } else {
                    StormboundIslesMod.LOGGER.warn("Islands file {} was empty or contained null data.", islandsPath.getFileName());
                }
            } catch (IOException e) {
                StormboundIslesMod.LOGGER.error("Could not read islands file: {}", islandsPath, e);
            } catch (JsonParseException e) {
                StormboundIslesMod.LOGGER.error("Invalid JSON format in islands file: {}", islandsPath, e);
            } catch (Exception e) {
                StormboundIslesMod.LOGGER.error("Unexpected error loading islands from file: {}", islandsPath, e);
            }
        } else {
            StormboundIslesMod.LOGGER.info("Islands file {} not found. Starting with empty island data.", islandsPath.getFileName());
        }
    }

    /**
     * Loads teams data from the specified JSON file.
     * Clears current in-memory teams before loading. Handles file reading and JSON parsing errors.
     *
     * @param dataDir The data directory containing the teams file. Must not be null.
     */
    private static void loadTeams(@NotNull Path dataDir) {
        Path teamsPath = dataDir.resolve(TEAMS_FILENAME);
        teams.clear();

        if (Files.exists(teamsPath)) {
            try (var reader = Files.newBufferedReader(teamsPath, StandardCharsets.UTF_8)) {
                Map<String, Team> loadedTeams = GSON.fromJson(reader, TEAM_MAP_TYPE);
                if (loadedTeams != null) {
                    teams.putAll(loadedTeams);
                    StormboundIslesMod.LOGGER.info("Loaded {} teams from {}", teams.size(), teamsPath.getFileName());
                } else {
                    StormboundIslesMod.LOGGER.warn("Teams file {} was empty or contained null data.", teamsPath.getFileName());
                }
            } catch (IOException e) {
                StormboundIslesMod.LOGGER.error("Could not read teams file: {}", teamsPath, e);
            } catch (JsonParseException e) {
                StormboundIslesMod.LOGGER.error("Invalid JSON format in teams file: {}", teamsPath, e);
            } catch (Exception e) {
                StormboundIslesMod.LOGGER.error("Unexpected error loading teams from file: {}", teamsPath, e);
            }
        } else {
            StormboundIslesMod.LOGGER.info("Teams file {} not found. Starting with empty team data.", teamsPath.getFileName());
        }
    }

    /**
     * Loads game state data from the specified JSON file and updates {@link GameManager}.
     * Handles file reading and JSON parsing errors.
     *
     * @param dataDir The data directory containing the game state file. Must not be null.
     */
    private static void loadGameState(@NotNull Path dataDir) {
        Path gameStatePath = dataDir.resolve(GAME_STATE_FILENAME);

        if (Files.exists(gameStatePath)) {
            try (var reader = Files.newBufferedReader(gameStatePath, StandardCharsets.UTF_8)) {
                GameState gameState = GSON.fromJson(reader, GameState.class);
                if (gameState != null && gameState.phase != null) {
                    GameManager.setPhaseWithoutReset(gameState.phase, gameState.phaseTicks);
                    StormboundIslesMod.LOGGER.info("Loaded game state from {}: Phase={}, Ticks={}",
                            gameStatePath.getFileName(), gameState.phase, gameState.phaseTicks);
                } else {
                    StormboundIslesMod.LOGGER.warn("Game state file {} was empty or contained invalid data. Using default game state.", gameStatePath.getFileName());
                    GameManager.setPhaseWithoutReset(GamePhase.LOBBY, 0);
                }
            } catch (IOException e) {
                StormboundIslesMod.LOGGER.error("Could not read game state file: {}", gameStatePath, e);
            } catch (JsonParseException e) {
                StormboundIslesMod.LOGGER.error("Invalid JSON format in game state file: {}", gameStatePath, e);
            } catch (Exception e) {
                StormboundIslesMod.LOGGER.error("Unexpected error loading game state from file: {}", gameStatePath, e);
            }
        } else {
            StormboundIslesMod.LOGGER.info("Game state file {} not found. Game will start in default state (LOBBY).", gameStatePath.getFileName());
            GameManager.setPhaseWithoutReset(GamePhase.LOBBY, 0);
        }
    }

    /**
     * Saves the current islands data to the specified JSON file.
     *
     * @param dataDir The data directory where the islands file should be saved. Must not be null.
     */
    private static void saveIslands(@NotNull Path dataDir) {
        Path islandsPath = dataDir.resolve(ISLANDS_FILENAME);
        writeJsonToFile(islandsPath, islands,
                (success) -> StormboundIslesMod.LOGGER.debug("Saved {} islands to {}", islands.size(), islandsPath.getFileName()),
                "islands"
        );
    }

    /**
     * Saves the current teams data to the specified JSON file.
     *
     * @param dataDir The data directory where the teams file should be saved. Must not be null.
     */
    private static void saveTeams(@NotNull Path dataDir) {
        Path teamsPath = dataDir.resolve(TEAMS_FILENAME);
        writeJsonToFile(teamsPath, teams,
                (success) -> StormboundIslesMod.LOGGER.debug("Saved {} teams to {}", teams.size(), teamsPath.getFileName()),
                "teams"
        );
    }

    /**
     * Saves the current game state data to the specified JSON file.
     *
     * @param dataDir The data directory where the game state file should be saved. Must not be null.
     */
    private static void saveGameState(@NotNull Path dataDir) {
        Path gameStatePath = dataDir.resolve(GAME_STATE_FILENAME);
        GameState gameState = new GameState(
                GameManager.phase,
                GameManager.getPhaseTicks()
        );
        writeJsonToFile(gameStatePath, gameState,
                (success) -> StormboundIslesMod.LOGGER.debug("Saved game state to {}: Phase={}, Ticks={}",
                        gameStatePath.getFileName(), gameState.phase, gameState.phaseTicks),
                "game state"
        );
    }

    /**
     * Utility method to write an object as JSON to a file using Gson.
     * Handles potential IO and JSON exceptions during the write process.
     *
     * @param path      The {@link Path} where the file should be written. Must not be null.
     * @param object    The object to serialize to JSON.
     * @param onSuccess Optional callback {@link Consumer} executed on successful write.
     * @param dataType  A descriptive name of the data type being saved (for logging). Must not be null.
     */
    private static void writeJsonToFile(@NotNull Path path, @Nullable Object object, @Nullable Consumer<Boolean> onSuccess, @NotNull String dataType) {
        try {
            String json = GSON.toJson(object);

            try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write(json);
            }

            if (onSuccess != null) {
                onSuccess.accept(true);
            }
        } catch (IOException e) {
            StormboundIslesMod.LOGGER.error("Failed to write {} data to file: {}", dataType, path, e);
        } catch (JsonParseException e) {
            StormboundIslesMod.LOGGER.error("Error generating JSON for {} data for file: {}", dataType, path, e);
        } catch (Exception e) {
            StormboundIslesMod.LOGGER.error("Unexpected error saving {} data to file: {}", dataType, path, e);
        }
    }

    // --- Inner Classes ---

    /**
     * Simple Data Transfer Object (DTO) or Record for serializing/deserializing game state.
     * Using a record for conciseness and immutability (Java 16+).
     * If targeting lower Java versions, revert to a private static class with fields and constructor.
     */
    private record GameState(GamePhase phase, int phaseTicks) {
    }
}