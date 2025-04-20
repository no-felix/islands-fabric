package de.felix.stormboundisles.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.felix.stormboundisles.teams.Team;
import de.felix.stormboundisles.teams.TeamManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and saves mod configuration (teams, zones, disasters, buffs, points).
 */
public class ConfigLoader {
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private final Type mapType = new TypeToken<HashMap<String, Object>>() {
	}.getType();

	public void loadConfig(File file) throws IOException {
		try (FileReader reader = new FileReader(file)) {
			Map<String, Object> config = gson.fromJson(reader, mapType);

			// Teams
			List<Map<String, String>> teams = (List<Map<String, String>>) config.get("teams");
			if (teams != null) {
				for (Map<String, String> entry : teams) {
					TeamManager.getInstance().addTeam(new Team(
							entry.get("name"),
							entry.get("color"),
							entry.get("islandType")
					));
				}
			}

			// Zones, Disasters, Buffs, Points, Bonus: deserialize analog
		}
	}

	public void saveConfig(File file) throws IOException {
		Map<String, Object> config = new HashMap<>();
		// Teams
		List<Team> teams = List.copyOf(TeamManager.getInstance().getAllTeams());
		config.put("teams", teams.stream().map(team -> {
			Map<String, String> entry = new HashMap<>();
			entry.put("name", team.getName());
			entry.put("color", team.getColorCode());
			entry.put("islandType", team.getIslandType());
			return entry;
		}).toList());

		// TODO: Add Zones, Disasters, Buffs, Points, Bonus serialization

		try (FileWriter writer = new FileWriter(file)) {
			gson.toJson(config, writer);
		}
	}
}