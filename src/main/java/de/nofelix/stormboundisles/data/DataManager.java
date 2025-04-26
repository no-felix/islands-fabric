package de.nofelix.stormboundisles.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.nofelix.stormboundisles.StormboundIslesMod;
import de.nofelix.stormboundisles.game.GameManager;
import de.nofelix.stormboundisles.game.GamePhase;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
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
	public static Map<String, Team> teams = new HashMap<>();
	/** In-memory map storing island data, keyed by island ID. Loaded from `islands.json`. */
	public static Map<String, Island> islands = new HashMap<>();

	/**
	 * Loads all game data (teams, islands, game state) from their respective JSON files
	 * located in the `world/stormboundisles` directory.
	 * If files don't exist or fail to load, initializes with empty data structures or defaults.
	 */
	public static void loadAll() {
		StormboundIslesMod.LOGGER.info("Loading game data");
		try {
			File teamFile = getFile("teams.json");
			File islandFile = getFile("islands.json");
			if (teamFile.exists()) {
				teams = GSON.fromJson(new FileReader(teamFile), TEAM_MAP_TYPE);
				StormboundIslesMod.LOGGER.info("Loaded {} teams", teams.size());
			} else {
				StormboundIslesMod.LOGGER.info("No teams file exists, creating empty teams data");
			}
			if (islandFile.exists()) {
				islands = GSON.fromJson(new FileReader(islandFile), ISLAND_MAP_TYPE);
				StormboundIslesMod.LOGGER.info("Loaded {} islands", islands.size());
			} else {
				StormboundIslesMod.LOGGER.info("No islands file exists, creating empty islands data");
			}
		} catch (Exception e) {
			StormboundIslesMod.LOGGER.error("Failed to load data", e);
		}
		if (teams == null) teams = new HashMap<>();
		if (islands == null) islands = new HashMap<>();
		// Load saved game phase and tick count
		loadGameState();
	}

	/**
	 * Internal class representing the structure of the saved game state file (`game_state.json`).
	 */
	private static class GameState {
		/** The name of the current game phase (e.g., "BUILD", "PVP"). */
		public String phase;
		/** The number of ticks elapsed in the current game phase. */
		public int phaseTicks;
	}

	/**
	 * Saves the current game state (phase and phase ticks) to `game_state.json`.
	 * Retrieves the current state from {@link GameManager}.
	 */
	public static void saveGameState() {
		try {
			File file = getFile("game_state.json");
			FileWriter writer = new FileWriter(file);
			GameState state = new GameState();
			state.phase = GameManager.phase.name();
			state.phaseTicks = GameManager.getPhaseTicks();
			GSON.toJson(state, GameState.class, writer);
			writer.close();
			StormboundIslesMod.LOGGER.info("Saved game state: {} @ {} ticks", state.phase, state.phaseTicks);
		} catch (Exception e) {
			StormboundIslesMod.LOGGER.error("Failed to save game state", e);
		}
	}

	/**
	 * Loads the game state (phase and phase ticks) from `game_state.json`.
	 * If the file exists and contains valid data, updates the {@link GameManager}
	 * using {@link GameManager#setPhaseWithoutReset(GamePhase, int)}.
	 */
	private static void loadGameState() {
		try {
			File file = getFile("game_state.json");
			if (file.exists()) {
				GameState state = GSON.fromJson(new FileReader(file), GameState.class);
				if (state != null) {
					GameManager.setPhaseWithoutReset(GamePhase.valueOf(state.phase), state.phaseTicks);
					StormboundIslesMod.LOGGER.info("Loaded game state: {} @ {} ticks", state.phase, state.phaseTicks);
				}
			}
		} catch (Exception e) {
			StormboundIslesMod.LOGGER.error("Failed to load game state", e);
		}
	}

	/**
	 * Saves all current in-memory game data (teams and islands) to their respective
	 * JSON files (`teams.json`, `islands.json`).
	 */
	public static void saveAll() {
		StormboundIslesMod.LOGGER.info("Saving game data: {} teams, {} islands", teams.size(), islands.size());
		try {
			File teamFile = getFile("teams.json");
			File islandFile = getFile("islands.json");
			FileWriter teamWriter = new FileWriter(teamFile);
			FileWriter islandWriter = new FileWriter(islandFile);
			GSON.toJson(teams, TEAM_MAP_TYPE, teamWriter);
			GSON.toJson(islands, ISLAND_MAP_TYPE, islandWriter);
			teamWriter.close();
			islandWriter.close();
		} catch (Exception e) {
			StormboundIslesMod.LOGGER.error("Failed to save data", e);
		}
	}

	/**
	 * Gets a {@link File} handle within the mod's data directory (`world/stormboundisles`).
	 * Creates the directory if it doesn't exist.
	 *
	 * @param name The name of the file (e.g., "teams.json").
	 * @return A File object representing the requested file path.
	 */
	private static File getFile(String name) {
		File dir = new File("world/stormboundisles");
		if (!dir.exists()) {
			StormboundIslesMod.LOGGER.info("Creating data directory: {}", dir.getPath());
			dir.mkdirs();
		}
		return new File(dir, name);
	}
}