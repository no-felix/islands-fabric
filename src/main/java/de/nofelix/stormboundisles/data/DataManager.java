package de.nofelix.stormboundisles.data;

import com.google.gson.Gson;
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
 * Manages loading and saving game data like teams, islands, and game state to files.
 */
public class DataManager {
	/** Gson instance for JSON serialization/deserialization. */
	private static final Gson GSON = new Gson();
	/** Type token for deserializing the map of teams. */
	private static final Type TEAM_MAP_TYPE = new TypeToken<Map<String, Team>>() {
	}.getType();
	/** Type token for deserializing the map of islands. */
	private static final Type ISLAND_MAP_TYPE = new TypeToken<Map<String, Island>>() {
	}.getType();
	/** In-memory map storing team data, keyed by team name. */
	public static Map<String, Team> teams = new HashMap<>();
	/** In-memory map storing island data, keyed by island ID. */
	public static Map<String, Island> islands = new HashMap<>();

	/**
	 * Loads all game data (teams, islands, game state) from their respective files.
	 * If files don't exist, initializes with empty data structures.
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
	 * Internal class to represent the saved game state.
	 */
	private static class GameState {
		/** The name of the current game phase. */
		public String phase;
		/** The number of ticks elapsed in the current game phase. */
		public int phaseTicks;
	}

	/**
	 * Saves the current game state (phase and phase ticks) to a JSON file.
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
	 * Loads the game state (phase and phase ticks) from its JSON file.
	 * If the file exists and contains valid data, updates the {@link GameManager}.
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
	 * Saves all current game data (teams and islands) to their respective JSON files.
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
	 * Gets a file handle within the mod's data directory inside the world save.
	 * Creates the directory if it doesn't exist.
	 *
	 * @param name The name of the file.
	 * @return A File object representing the requested file.
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