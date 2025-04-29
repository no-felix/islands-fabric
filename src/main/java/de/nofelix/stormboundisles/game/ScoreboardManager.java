package de.nofelix.stormboundisles.game;

import de.nofelix.stormboundisles.StormboundIslesMod;
import de.nofelix.stormboundisles.data.DataManager;
import de.nofelix.stormboundisles.data.Team;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.UUID;

/**
 * Manages the scoreboard display for the Stormbound Isles mod.
 * This class handles the creation, initialization, and updating of the
 * scoreboard objective that displays team points in the sidebar.
 * It ensures that team scores are correctly represented with appropriate
 * display names and colors.
 */
public class ScoreboardManager {
	private static final String OBJECTIVE_NAME = "sbi_points";
	private static Scoreboard scoreboard;
	private static ScoreboardObjective objective;

	public static void register() {
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(ScoreboardManager::initialize);

		net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 20 == 0) {
				updateAllScores();
			}
		});

		// Update team entries when a player joins
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> updateAllTeams(server));

		StormboundIslesMod.LOGGER.info("ScoreboardManager registered");
	}

	/**
	 * Initializes the scoreboard manager.
	 * Gets the server scoreboard, removes any existing objective with the same name,
	 * creates a new objective for team points, sets it to display in the sidebar,
	 * and updates all team scores initially.
	 *
	 * @param server The Minecraft server instance.
	 */
	public static void initialize(MinecraftServer server) {
		scoreboard = server.getScoreboard();

		// Remove existing objective if present
		ScoreboardObjective existingObjective = scoreboard.getNullableObjective(OBJECTIVE_NAME);
		if (existingObjective != null) {
			scoreboard.removeObjective(existingObjective);
		}

		// Create new objective
		objective = scoreboard.addObjective(
				OBJECTIVE_NAME,
				ScoreboardCriterion.DUMMY,
				Text.literal("§b§lStormbound Isles"),
				ScoreboardCriterion.RenderType.INTEGER,
				false, // displayAutoUpdate
				null   // numberFormat
		);

		// Display objective in the sidebar slot
		scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, objective);

		// Setup scoreboard teams for playerlist & chat formatting
		setupScoreboardTeams(server);

		// Update all team scores
		updateAllScores();

		StormboundIslesMod.LOGGER.info("Scoreboard initialized");
	}

	/**
	 * Updates all scores on the scoreboard.
	 * Clears existing scores for the objective and then sets the score
	 * for each team based on the current data in DataManager.
	 */
	public static void updateAllScores() {
		if (scoreboard == null || objective == null) return;

		// Remove all existing scores for this objective
		for (ScoreHolder holder : scoreboard.getKnownScoreHolders()) {
			// Check if the score belongs to our objective before removing
			if (scoreboard.getScore(holder, objective) != null) {
				scoreboard.removeScore(holder, objective);
			}
		}

		// Set scores for all teams
		for (Map.Entry<String, Team> entry : DataManager.getTeams().entrySet()) {
			Team team = entry.getValue();
			String displayName = getDisplayNameForTeam(team);
			ScoreAccess score = scoreboard.getOrCreateScore(ScoreHolder.fromName(displayName), objective);
			score.setScore(team.getPoints());
		}
	}

	/**
	 * Updates the score for a specific team on the scoreboard.
	 *
	 * @param teamName The name of the team whose score needs updating.
	 */
	public static void updateTeamScore(String teamName) {
		if (scoreboard == null || objective == null) return;

		Team team = DataManager.getTeam(teamName);
		if (team == null) return;

		String displayName = getDisplayNameForTeam(team);
		ScoreAccess score = scoreboard.getOrCreateScore(ScoreHolder.fromName(displayName), objective);
		score.setScore(team.getPoints());
	}

	/**
	 * Gets the display name for a team, including color codes.
	 *
	 * @param team The team object.
	 * @return The formatted display name string with color codes.
	 */
	private static String getDisplayNameForTeam(Team team) {
		// Colored team names based on the name
		return switch (team.getName()) {
			case "VOLCANO" -> "§c" + team.getName();
			case "ICE" -> "§b" + team.getName();
			case "DESERT" -> "§e" + team.getName();
			case "MUSHROOM" -> "§d" + team.getName();
			case "CRYSTAL" -> "§9" + team.getName();
			default -> team.getName(); // Default case without color
		};
	}

	/**
	 * Create or update scoreboard teams for each custom team.
	 *
	 * @param server The Minecraft server instance.
	 */
	private static void setupScoreboardTeams(MinecraftServer server) {
		for (Map.Entry<String, Team> entry : DataManager.getTeams().entrySet()) {
			String teamName = entry.getKey();
			Team team = entry.getValue();

			// Get or create the scoreboard team
			net.minecraft.scoreboard.Team sbTeam = scoreboard.getTeam(teamName);
			if (sbTeam == null) {
				sbTeam = scoreboard.addTeam(teamName);
			}

			// Set display settings
			sbTeam.setDisplayName(Text.literal(teamName));
			sbTeam.setPrefix(Text.literal(getDisplayNameForTeam(team) + " §r"));

			// Remove old members
			for (String memberName : sbTeam.getPlayerList()) {
				scoreboard.removeScoreHolderFromTeam(memberName, sbTeam);
			}

			// Add current members
			for (UUID uuid : team.getMembers()) {
				ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
				if (player != null) {
					scoreboard.addScoreHolderToTeam(player.getGameProfile().getName(), sbTeam);
				}
			}
		}
	}

	/**
	 * Rebuilds all scoreboard team assignments on the playerlist and chat.
	 *
	 * @param server The Minecraft server instance.
	 */
	public static void updateAllTeams(MinecraftServer server) {
		setupScoreboardTeams(server);
	}
}
