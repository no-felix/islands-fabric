package de.nofelix.stormboundisles.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.nofelix.stormboundisles.StormboundIslesMod;
import de.nofelix.stormboundisles.game.GameManager;
import de.nofelix.stormboundisles.game.GamePhase;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages loading and saving persistent game data, including team information,
 * island definitions, and the current game state (phase and progress).
 * Data is stored in JSON files within the world save directory (`world/stormboundisles`).
 */
public class DataManager {
	/** Gson instance for JSON serialization/deserialization with pretty printing. */
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	/** Type token for deserializing the map of teams (String teamName -> Team object). */
	private static final Type TEAM_MAP_TYPE = new TypeToken<Map<String, Team>>() {
	}.getType();
	/** Type token for deserializing the map of islands (String islandId -> Island object). */
	private static final Type ISLAND_MAP_TYPE = new TypeToken<Map<String, Island>>() {
	}.getType();
	/** In-memory map storing team data, keyed by team name. Loaded from `teams.json`. */
	private static Map<String, Team> teams = new HashMap<>();
	/** In-memory map storing island data, keyed by island ID. Loaded from `islands.json`. */
	private static Map<String, Island> islands = new HashMap<>();

	/**
	 * Returns an unmodifiable view of the teams map.
	 * 
	 * @return An unmodifiable map of teams
	 */
	public static Map<String, Team> getTeams() {
		return Map.copyOf(teams);
	}

	/**
	 * Returns an unmodifiable view of the islands map.
	 * 
	 * @return An unmodifiable map of islands
	 */
	public static Map<String, Island> getIslands() {
		return Map.copyOf(islands);
	}

	/**
	 * Gets a team by its name.
	 * 
	 * @param teamName The name of the team to retrieve
	 * @return The team object, or null if no team exists with the given name
	 */
	public static Team getTeam(String teamName) {
		return teams.get(teamName);
	}

	/**
	 * Gets an island by its ID.
	 * 
	 * @param islandId The ID of the island to retrieve
	 * @return The island object, or null if no island exists with the given ID
	 */
	public static Island getIsland(String islandId) {
		return islands.get(islandId);
	}

	/**
	 * Adds or updates a team in the teams collection.
	 * 
	 * @param team The team to add or update
	 */
	public static void putTeam(Team team) {
		if (team == null || team.getName() == null) {
			throw new IllegalArgumentException("Cannot add a null team or team with null name");
		}
		teams.put(team.getName(), team);
	}

	/**
	 * Adds or updates an island in the islands collection.
	 * 
	 * @param island The island to add or update
	 */
	public static void putIsland(Island island) {
		if (island == null || island.getId() == null) {
			throw new IllegalArgumentException("Cannot add a null island or island with null id");
		}
		islands.put(island.getId(), island);
	}

	/**
	 * Clears all teams from the teams collection.
	 */
	public static void clearTeams() {
		teams.clear();
	}

	/**
	 * Clears all islands from the islands collection.
	 */
	public static void clearIslands() {
		islands.clear();
	}

	/**
	 * Loads all data from the default directory.
	 * Convenience method that uses the game directory from FabricLoader.
	 */
	public static void loadAll() {
		Path runDir = FabricLoader.getInstance().getGameDir();
		load(runDir);
	}

	/**
	 * Loads all data from the appropriate JSON files.
	 * If files don't exist or are invalid, initializes with empty collections.
	 * 
	 * @param runDir The directory where the game is running
	 */
	public static void load(@NotNull Path runDir) {
		StormboundIslesMod.LOGGER.info("Loading data from {}", runDir);
		try {
			Path dataDir = ensureDataDirectory(runDir);
			
			// Load islands first since teams reference them
			try {
				Path islandsPath = dataDir.resolve("islands.json");
				if (Files.exists(islandsPath)) {
					String islandsJson = Files.readString(islandsPath, StandardCharsets.UTF_8);
					islands = GSON.fromJson(islandsJson, ISLAND_MAP_TYPE);
					StormboundIslesMod.LOGGER.info("Loaded {} islands", islands.size());
				}
			} catch (Exception e) {
				StormboundIslesMod.LOGGER.error("Failed to load islands", e);
				islands = new HashMap<>();
			}
			
			// Load teams
			try {
				Path teamsPath = dataDir.resolve("teams.json");
				if (Files.exists(teamsPath)) {
					String teamsJson = Files.readString(teamsPath, StandardCharsets.UTF_8);
					teams = GSON.fromJson(teamsJson, TEAM_MAP_TYPE);
					StormboundIslesMod.LOGGER.info("Loaded {} teams", teams.size());
				}
			} catch (Exception e) {
				StormboundIslesMod.LOGGER.error("Failed to load teams", e);
				teams = new HashMap<>();
			}
			
			// Load game state
			try {
				Path gameStatePath = dataDir.resolve("game_state.json");
				if (Files.exists(gameStatePath)) {
					String gameStateJson = Files.readString(gameStatePath, StandardCharsets.UTF_8);
					GameState gameState = GSON.fromJson(gameStateJson, GameState.class);
					if (gameState != null) {
						GameManager.setPhaseWithoutReset(gameState.phase, gameState.phaseTicks);
						StormboundIslesMod.LOGGER.info("Loaded game state: {} (ticks: {})", 
								gameState.phase, gameState.phaseTicks);
					}
				}
			} catch (Exception e) {
				StormboundIslesMod.LOGGER.error("Failed to load game state", e);
			}
		} catch (Exception e) {
			StormboundIslesMod.LOGGER.error("Error during data loading", e);
		}
	}

	/**
	 * Creates and ensures the existence of the data directory for the mod.
	 * 
	 * @param runDir The base directory for the game
	 * @return The path to the data directory
	 * @throws IOException If directory creation fails
	 */
	private static Path ensureDataDirectory(Path runDir) throws IOException {
		Path worldDir = runDir.resolve("world");
		if (!Files.exists(worldDir)) {
			worldDir = runDir;  // No world directory, use run directory directly
		}
		Path dataDir = worldDir.resolve("stormboundisles");
		if (!Files.exists(dataDir)) {
			Files.createDirectories(dataDir);
			StormboundIslesMod.LOGGER.info("Created data directory: {}", dataDir);
		}
		return dataDir;
	}

	/**
	 * Saves all data to the appropriate JSON files.
	 */
	public static void saveAll() {
		Path runDir = FabricLoader.getInstance().getGameDir();
		saveAll(runDir);
	}

	/**
	 * Saves all data to the appropriate JSON files in the specified directory.
	 * 
	 * @param runDir The directory where the game is running
	 */
	public static void saveAll(Path runDir) {
		try {
			Path dataDir = ensureDataDirectory(runDir);
			
			// Save islands
			try {
				Path islandsPath = dataDir.resolve("islands.json");
				String islandsJson = GSON.toJson(islands);
				Files.writeString(islandsPath, islandsJson, StandardCharsets.UTF_8);
				StormboundIslesMod.LOGGER.debug("Saved {} islands", islands.size());
			} catch (Exception e) {
				StormboundIslesMod.LOGGER.error("Failed to save islands", e);
			}
			
			// Save teams
			try {
				Path teamsPath = dataDir.resolve("teams.json");
				String teamsJson = GSON.toJson(teams);
				Files.writeString(teamsPath, teamsJson, StandardCharsets.UTF_8);
				StormboundIslesMod.LOGGER.debug("Saved {} teams", teams.size());
			} catch (Exception e) {
				StormboundIslesMod.LOGGER.error("Failed to save teams", e);
			}
			
			// Save game state
			saveGameState();
		} catch (Exception e) {
			StormboundIslesMod.LOGGER.error("Error during data saving", e);
		}
	}

	/**
	 * Saves only the game state to its JSON file.
	 */
	public static void saveGameState() {
		Path runDir = FabricLoader.getInstance().getGameDir();
		try {
			Path dataDir = ensureDataDirectory(runDir);
			try {
				Path gameStatePath = dataDir.resolve("game_state.json");
				GameState gameState = new GameState(
						GameManager.phase, 
						GameManager.getPhaseTicks()
				);
				String gameStateJson = GSON.toJson(gameState);
				Files.writeString(gameStatePath, gameStateJson, StandardCharsets.UTF_8);
				StormboundIslesMod.LOGGER.debug("Saved game state: {} (ticks: {})", 
						gameState.phase, gameState.phaseTicks);
			} catch (Exception e) {
				StormboundIslesMod.LOGGER.error("Failed to save game state", e);
			}
		} catch (Exception e) {
			StormboundIslesMod.LOGGER.error("Error during game state saving", e);
		}
	}

	/**
	 * Simple DTO for serializing/deserializing game state.
	 */
	private static class GameState {
		public GamePhase phase;
		public int phaseTicks;

		public GameState(GamePhase phase, int phaseTicks) {
			this.phase = phase;
			this.phaseTicks = phaseTicks;
		}
	}
}