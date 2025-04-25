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
 * Manages loading and providing access to mod configuration.
 */
public final class ConfigManager {
	private static final String CONFIG_FILENAME = "stormbound-isles-config.json";
	private static final Gson GSON = new Gson();
	private static Config config;

	private ConfigManager() {
	}

	/**
	 * Loads the config from disk or creates a default one if missing or invalid.
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
	 * Interval (in ticks) between boundary checks.
	 */
	public static int getBoundaryCheckInterval() {
		return config.boundaryCheckInterval;
	}

	/**
	 * Interval (in seconds) between buff updates.
	 */
	public static int getBuffUpdateInterval() {
		return config.buffUpdateInterval;
	}

	/**
	 * Interval (in ticks) between disasters.
	 */
	public static int getDisasterIntervalTicks() {
		return config.disasterIntervalTicks;
	}

	/**
	 * Holds default and loaded configuration values.
	 */
	private static class Config {
		int boundaryCheckInterval = 10;
		int buffUpdateInterval = 60;
		int disasterIntervalTicks = 20 * 60 * 5;
	}
}
