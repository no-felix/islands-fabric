package de.nofelix.stormboundisles.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder; // Import GsonBuilder
import com.google.gson.JsonSyntaxException;
import de.nofelix.stormboundisles.StormboundIslesMod;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Manages loading, accessing, and saving the mod's configuration settings.
 * Configuration is loaded from `stormbound-isles-config.json` in the config
 * directory.
 * If the file doesn't exist, default values are used and a new file is created.
 */
public final class ConfigManager {
	/** The filename for the configuration file. */
	private static final String CONFIG_FILENAME = "stormbound-isles-config.json";
	/** Gson instance for JSON handling. */
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	/** The loaded configuration instance. */
	private static Config config;

	/** Private constructor to prevent instantiation of this utility class. */
	private ConfigManager() {
	}

	/**
	 * Loads the configuration from the JSON file.
	 * If the file is missing or corrupted, it creates a default configuration file
	 * and uses the default values.
	 */
	public static void loadConfig() {
		Logger log = StormboundIslesMod.LOGGER;
		File configFile = FabricLoader.getInstance()
				.getConfigDir()
				.resolve(CONFIG_FILENAME)
				.toFile();

		if (!configFile.exists()) {
			config = new Config(); // Create default config
			try (FileWriter writer = new FileWriter(configFile)) {
				GSON.toJson(config, writer);
				log.info("Created default config file: {}", CONFIG_FILENAME);
			} catch (IOException e) {
				log.error("Failed to write default config to {}", CONFIG_FILENAME, e);
			}
		} else {
			try (FileReader reader = new FileReader(configFile)) {
				config = GSON.fromJson(reader, Config.class);
				// Handle cases where the file exists but is empty or invalid JSON root
				if (config == null) {
					log.warn("Config file {} was empty or invalid. Creating default config.", CONFIG_FILENAME);
					config = new Config();
					// Optionally overwrite the invalid file with defaults
					try (FileWriter writer = new FileWriter(configFile)) {
						GSON.toJson(config, writer);
						log.info("Overwrote invalid config file {} with defaults.", CONFIG_FILENAME);
					} catch (IOException e) {
						log.error("Failed to overwrite invalid config file {} with defaults.", CONFIG_FILENAME, e);
					}
				} else {
					// Ensure nested objects are not null if the file was partially valid
					if (config.game == null)
						config.game = new Config.Game();
					if (config.player == null)
						config.player = new Config.Player();
					if (config.buff == null)
						config.buff = new Config.Buff();
					if (config.disaster == null)
						config.disaster = new Config.Disaster();
				}
			} catch (IOException | JsonSyntaxException e) {
				log.error("Failed to load {} (using defaults): {}", CONFIG_FILENAME, e.getMessage());
				config = new Config(); // Fallback to defaults on error
			}
		}

		// Log loaded/default values (consider logging nested structure)
		log.info(
				"Config loaded. Game(BuildTicks={}, PvpTicks={}, CountdownTicks={}), Player(BoundaryInterval={}, DeathPenalty={}, WarningCooldownMs={}, ResetConfirmationTimeoutMs={}), Buff(UpdateInterval={}, DurationTicks={}), Disaster(IntervalTicks={}, EffectDurationTicks={}, CooldownTicks={}, MeteorDamage={}, BlizzardFreezeTicks={})",
				config.game.buildPhaseTicks, config.game.pvpPhaseTicks, config.game.countdownDurationTicks,
				config.player.boundaryCheckInterval, config.player.deathPenalty,
				config.player.boundaryWarningCooldownMs, config.player.resetConfirmationTimeoutMs,
				config.buff.buffUpdateInterval, config.buff.buffDurationTicks,
				config.disaster.disasterIntervalTicks, config.disaster.disasterEffectDurationTicks,
				config.disaster.disasterCooldownTicks, config.disaster.meteorDamage,
				config.disaster.blizzardFreezeTicks);
	}

	// --- Getters for Config Values ---

	// Game Settings
	public static int getGameBuildPhaseTicks() {
		return config.game.buildPhaseTicks;
	}

	public static int getGamePvpPhaseTicks() {
		return config.game.pvpPhaseTicks;
	}

	public static int getGameCountdownDurationTicks() {
		return config.game.countdownDurationTicks;
	}

	// Player Settings
	public static int getPlayerBoundaryCheckInterval() {
		return config.player.boundaryCheckInterval;
	}

	public static int getPlayerDeathPenalty() {
		return config.player.deathPenalty;
	}

	public static long getPlayerBoundaryWarningCooldownMs() {
		return config.player.boundaryWarningCooldownMs;
	}

	public static long getPlayerResetConfirmationTimeoutMs() {
		return config.player.resetConfirmationTimeoutMs;
	}

	// Buff Settings
	public static int getBuffUpdateInterval() {
		return config.buff.buffUpdateInterval;
	}

	public static int getBuffDurationTicks() {
		return config.buff.buffDurationTicks;
	}

	// Disaster Settings
	public static int getDisasterIntervalTicks() {
		return config.disaster.disasterIntervalTicks;
	}

	public static int getDisasterEffectDurationTicks() {
		return config.disaster.disasterEffectDurationTicks;
	}

	public static int getDisasterCooldownTicks() {
		return config.disaster.disasterCooldownTicks;
	}

	public static float getDisasterMeteorDamage() {
		return config.disaster.meteorDamage;
	}

	public static int getDisasterBlizzardFreezeTicks() {
		return config.disaster.blizzardFreezeTicks;
	}

	/**
	 * Root class representing the structure of the configuration file.
	 * Contains nested classes for different setting categories.
	 */
	private static class Config {
		Game game = new Game();
		Player player = new Player();
		Buff buff = new Buff();
		Disaster disaster = new Disaster();

		/** Game-related settings like phase durations and countdowns. */
		static class Game {
			/** Duration of the build phase in ticks. Default: 12096000 ticks (1 week). */
			int buildPhaseTicks = 20 * 60 * 60 * 24 * 7; // 12096000
			/** Duration of the PvP phase in ticks. Default: 12096000 ticks (1 week). */
			int pvpPhaseTicks = 20 * 60 * 60 * 24 * 7; // 12096000
			/**
			 * Duration in ticks for the pre-game countdown. Default: 200 ticks (10
			 * seconds).
			 */
			int countdownDurationTicks = 20 * 10; // 200
		}

		/** Player-related settings like boundary checks and penalties. */
		static class Player {
			/**
			 * Interval in ticks for checking if players are outside their island
			 * boundaries. Default: 10 ticks.
			 */
			int boundaryCheckInterval = 10;
			/** Points deducted from a team when a member dies. Default: 10 points. */
			int deathPenalty = 10;
			/**
			 * Cooldown in milliseconds before a player receives another boundary warning
			 * message. Default: 3000ms (3 seconds).
			 */
			long boundaryWarningCooldownMs = 3000L;
			/**
			 * Time window in milliseconds for confirming destructive actions like data
			 * reset. Default: 10000ms (10 seconds).
			 */
			long resetConfirmationTimeoutMs = 10000L;
		}

		/** Settings related to island buffs. */
		static class Buff {
			/**
			 * Interval in ticks for applying island-specific buffs. Default: 60 ticks (3
			 * seconds).
			 */
			int buffUpdateInterval = 60;
			/** Duration in ticks for island buffs. Default: 80 ticks (4 seconds). */
			int buffDurationTicks = 80;
		}

		/** Settings related to disasters. */
		static class Disaster {
			/**
			 * Interval in ticks for attempting to trigger a random disaster. Default: 6000
			 * ticks (5 minutes).
			 */
			int disasterIntervalTicks = 20 * 60 * 5; // 6000
			/**
			 * Duration in ticks for disaster status effects (Blindness, Poison,
			 * Levitation). Default: 100 ticks (5 seconds).
			 */
			int disasterEffectDurationTicks = 100;
			/**
			 * Cooldown in ticks before a disaster can re-trigger on the same island.
			 * Default: 100 ticks (5 seconds).
			 */
			int disasterCooldownTicks = 100;
			/**
			 * Damage dealt by a single meteor hit during the METEOR disaster. Default: 8.0F
			 * (4 hearts).
			 */
			float meteorDamage = 8.0F;
			/**
			 * Additional freeze ticks applied by the BLIZZARD disaster. Default: 200 ticks
			 * (10 seconds).
			 */
			int blizzardFreezeTicks = 200;
		}
	}
}
