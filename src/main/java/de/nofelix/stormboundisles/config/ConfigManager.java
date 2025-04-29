package de.nofelix.stormboundisles.config;

import com.google.gson.Gson;
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
 * Configuration is loaded from `stormbound-isles-config.json` in the config directory.
 * If the file doesn't exist, default values are used and a new file is created.
 */
public final class ConfigManager {
	/** The filename for the configuration file. */
	private static final String CONFIG_FILENAME = "stormbound-isles-config.json";
	/** Gson instance for JSON handling. */
	private static final Gson GSON = new Gson();
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
			config = new Config();
			try (FileWriter writer = new FileWriter(configFile)) {
				GSON.toJson(config, writer);
			} catch (IOException e) {
				log.error("Failed to write default config to {}", CONFIG_FILENAME, e);
			}
		} else {
			try (FileReader reader = new FileReader(configFile)) {
				config = GSON.fromJson(reader, Config.class);
			} catch (IOException | JsonSyntaxException e) {
				log.error("Failed to load {} (using defaults)", CONFIG_FILENAME, e);
				config = new Config();
			}
		}

		log.info(
				"Config loaded: boundaryInterval={}, buffInterval={}, disasterIntervalTicks={}",
				config.boundaryCheckInterval,
				config.buffUpdateInterval,
				config.disasterIntervalTicks
		);
	}

	/**
	 * Gets the configured interval (in ticks) between player boundary checks.
	 * @return The boundary check interval in ticks.
	 */
	public static int getBoundaryCheckInterval() {
		return config.boundaryCheckInterval;
	}

	/**
	 * Gets the configured interval (in ticks) between applying island buffs.
	 * @return The buff update interval in ticks.
	 */
	public static int getBuffUpdateInterval() {
		return config.buffUpdateInterval;
	}

	/**
	 * Gets the configured interval (in ticks) between potential disaster events.
	 * @return The disaster interval in ticks.
	 */
	public static int getDisasterIntervalTicks() {
		return config.disasterIntervalTicks;
	}

	/**
	 * Internal class representing the structure of the configuration file.
	 * Contains default values for all settings.
	 */
	private static class Config {
		/** Interval in ticks for checking if players are outside their island boundaries. Default: 10 ticks. */
		int boundaryCheckInterval = 10;
		/** Interval in ticks for applying island-specific buffs. Default: 60 ticks (3 seconds). */
		int buffUpdateInterval = 60;
		/** Interval in ticks for attempting to trigger a random disaster. Default: 6000 ticks (5 minutes). */
		int disasterIntervalTicks = 20 * 60 * 5;
	}
}
