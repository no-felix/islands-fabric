package de.nofelix.stormboundisles.game;

import de.nofelix.stormboundisles.StormboundIslesMod;
import de.nofelix.stormboundisles.data.DataManager;
import de.nofelix.stormboundisles.data.Team;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.UUID;

/**
 * Manages the scoreboard display and team assignments for the Stormbound Isles mod.
 * Handles scoreboard objectives for points and synchronizes Minecraft scoreboard teams
 * with the custom team data stored in DataManager.
 */
public class ScoreboardManager {
	private static final String OBJECTIVE_NAME = "sbi_points";
	private static Scoreboard scoreboard;
	private static ScoreboardObjective objective;
	private static MinecraftServer currentServer;

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			currentServer = server;
			initialize(server);
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (currentServer == null) currentServer = server;

			if (server.getTicks() % 20 == 0) {
				if (scoreboard != null && objective != null) {
					updateAllScores();
				} else if (currentServer != null) {
					StormboundIslesMod.LOGGER.warn("Scoreboard or objective is null, attempting re-initialization.");
					initialize(currentServer);
				}
			}
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			currentServer = server;
			addPlayerToScoreboardTeamOnJoin(handler.player);
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			currentServer = server;
			removePlayerFromScoreboardTeamOnLeave(handler.player);
		});

		StormboundIslesMod.LOGGER.info("ScoreboardManager registered event listeners.");
	}

	public static void initialize(MinecraftServer server) {
		if (server == null) {
			StormboundIslesMod.LOGGER.error("Cannot initialize ScoreboardManager: Server instance is null.");
			return;
		}
		currentServer = server;
		scoreboard = server.getScoreboard();
		if (scoreboard == null) {
			StormboundIslesMod.LOGGER.error("Cannot initialize ScoreboardManager: Scoreboard is null.");
			return;
		}

		StormboundIslesMod.LOGGER.info("Initializing Scoreboard...");

		ScoreboardObjective existingObjective = scoreboard.getNullableObjective(OBJECTIVE_NAME);
		if (existingObjective != null) {
			scoreboard.removeObjective(existingObjective);
			StormboundIslesMod.LOGGER.debug("Removed existing scoreboard objective: {}", OBJECTIVE_NAME);
		}

		try {
			objective = scoreboard.addObjective(
					OBJECTIVE_NAME,
					ScoreboardCriterion.DUMMY,
					Text.literal("§b§lStormbound Isles"),
					ScoreboardCriterion.RenderType.INTEGER,
					true,
					null
			);
			StormboundIslesMod.LOGGER.debug("Created scoreboard objective: {}", OBJECTIVE_NAME);
		} catch (IllegalArgumentException e) {
			objective = scoreboard.getNullableObjective(OBJECTIVE_NAME);
			if (objective == null) {
				StormboundIslesMod.LOGGER.error("Failed to create or get scoreboard objective: {}", OBJECTIVE_NAME, e);
				return;
			}
			StormboundIslesMod.LOGGER.warn("Scoreboard objective {} already existed.", OBJECTIVE_NAME);
		}

		scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, objective);
		StormboundIslesMod.LOGGER.debug("Set objective {} to display in sidebar.", OBJECTIVE_NAME);

		setupScoreboardTeamProperties();
		updateAllScores();

		StormboundIslesMod.LOGGER.debug("Assigning initially online players to scoreboard teams...");
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			addPlayerToScoreboardTeamOnJoin(player);
		}

		StormboundIslesMod.LOGGER.info("Scoreboard initialized successfully.");
	}

	public static void updateAllScores() {
		if (scoreboard == null || objective == null) {
			StormboundIslesMod.LOGGER.warn("Cannot update scores: Scoreboard or objective not initialized.");
			if (currentServer != null) {
				initialize(currentServer);
				if (scoreboard == null || objective == null) return;
			} else {
				return;
			}
		}

		for (Map.Entry<String, Team> entry : DataManager.getTeams().entrySet()) {
			Team team = entry.getValue();
			String displayName = getDisplayNameForTeam(team);
			ScoreAccess score = scoreboard.getOrCreateScore(ScoreHolder.fromName(displayName), objective);
			if (score != null) {
				score.setScore(team.getPoints());
			} else {
				StormboundIslesMod.LOGGER.warn("Could not get or create score for display name: {}", displayName);
			}
		}
	}

	public static void updateTeamScore(String teamName) {
		if (scoreboard == null || objective == null) {
			StormboundIslesMod.LOGGER.warn("Cannot update team score: Scoreboard or objective not initialized.");
			return;
		}

		Team team = DataManager.getTeam(teamName);
		if (team == null) {
			StormboundIslesMod.LOGGER.warn("Cannot update score for non-existent team: {}", teamName);
			return;
		}

		String displayName = getDisplayNameForTeam(team);
		ScoreAccess score = scoreboard.getOrCreateScore(ScoreHolder.fromName(displayName), objective);
		if (score != null) {
			score.setScore(team.getPoints());
		} else {
			StormboundIslesMod.LOGGER.warn("Could not get or create score for display name: {}", displayName);
		}
	}

	private static void setupScoreboardTeamProperties() {
		if (scoreboard == null) {
			StormboundIslesMod.LOGGER.warn("Cannot setup scoreboard team properties: Scoreboard not initialized.");
			return;
		}
		StormboundIslesMod.LOGGER.debug("Setting up scoreboard team properties...");

		for (Map.Entry<String, Team> entry : DataManager.getTeams().entrySet()) {
			String teamName = entry.getKey();
			Team teamData = entry.getValue();

			net.minecraft.scoreboard.Team sbTeam = scoreboard.getTeam(teamName);
			if (sbTeam == null) {
				sbTeam = scoreboard.addTeam(teamName);
				StormboundIslesMod.LOGGER.debug("Created scoreboard team: {}", teamName);
			} else {
				StormboundIslesMod.LOGGER.debug("Updating properties for scoreboard team: {}", teamName);
			}

			sbTeam.setDisplayName(Text.literal(teamName));
			Formatting color = getTeamColor(teamData);
			sbTeam.setColor(color);
			sbTeam.setPrefix(Text.literal("[").append(Text.literal(teamName).formatted(color)).append("] "));
			sbTeam.setFriendlyFireAllowed(false);
			sbTeam.setShowFriendlyInvisibles(true);
		}
		StormboundIslesMod.LOGGER.debug("Finished setting up scoreboard team properties.");
	}

	private static void addPlayerToScoreboardTeamOnJoin(ServerPlayerEntity player) {
		if (scoreboard == null || player == null) {
			StormboundIslesMod.LOGGER.warn("Cannot add player to scoreboard team: Scoreboard not initialized or player is null.");
			return;
		}

		UUID playerUuid = player.getUuid();
		String playerName = player.getGameProfile().getName();
		String targetTeamName = null;

		for (Team team : DataManager.getTeams().values()) {
			if (team.getMembers().contains(playerUuid)) {
				targetTeamName = team.getName();
				break;
			}
		}

		if (targetTeamName != null) {
			net.minecraft.scoreboard.Team sbTeam = scoreboard.getTeam(targetTeamName);
			if (sbTeam != null) {
				if (!sbTeam.getPlayerList().contains(playerName)) {
					scoreboard.addScoreHolderToTeam(playerName, sbTeam);
					StormboundIslesMod.LOGGER.info("Added player {} to scoreboard team {}", playerName, targetTeamName);
				} else {
					StormboundIslesMod.LOGGER.debug("Player {} already in scoreboard team {}", playerName, targetTeamName);
				}
			} else {
				StormboundIslesMod.LOGGER.warn("Scoreboard team {} not found for player {} (DataManager team exists). Re-running setup.", targetTeamName, playerName);
				setupScoreboardTeamProperties();
				sbTeam = scoreboard.getTeam(targetTeamName);
				if (sbTeam != null) {
					scoreboard.addScoreHolderToTeam(playerName, sbTeam);
					StormboundIslesMod.LOGGER.info("Added player {} to scoreboard team {} after re-setup.", playerName, targetTeamName);
				} else {
					StormboundIslesMod.LOGGER.error("Failed to find/create scoreboard team {} even after re-setup.", targetTeamName);
				}
			}
		} else {
			StormboundIslesMod.LOGGER.debug("Player {} not found in any DataManager team. Ensuring removal from scoreboard teams.", playerName);
			removePlayerFromAllScoreboardTeams(playerName);
		}
	}

	private static void removePlayerFromScoreboardTeamOnLeave(ServerPlayerEntity player) {
		if (scoreboard == null || player == null) {
			return;
		}
		removePlayerFromAllScoreboardTeams(player.getGameProfile().getName());
	}

	private static void removePlayerFromAllScoreboardTeams(String playerName) {
		if (scoreboard == null) return;

		for (String teamName : DataManager.getTeams().keySet()) {
			net.minecraft.scoreboard.Team sbTeam = scoreboard.getTeam(teamName);
			if (sbTeam != null && sbTeam.getPlayerList().contains(playerName)) {
				scoreboard.removeScoreHolderFromTeam(playerName, sbTeam);
				StormboundIslesMod.LOGGER.info("Removed player {} from scoreboard team {}", playerName, teamName);
			}
		}

		AbstractTeam playerTeam = scoreboard.getScoreHolderTeam(playerName);
		if (playerTeam instanceof net.minecraft.scoreboard.Team && DataManager.getTeam(playerTeam.getName()) == null) {
			scoreboard.removeScoreHolderFromTeam(playerName, (net.minecraft.scoreboard.Team) playerTeam);
			StormboundIslesMod.LOGGER.info("Removed player {} from non-DataManager scoreboard team {}.", playerName, playerTeam.getName());
		}
	}

	public static void updateAllTeams(MinecraftServer server) {
		if (server == null) {
			StormboundIslesMod.LOGGER.error("Cannot update all teams: Server instance is null.");
			return;
		}
		currentServer = server;
		if (scoreboard == null) {
			StormboundIslesMod.LOGGER.warn("Scoreboard not initialized during updateAllTeams. Attempting initialization.");
			initialize(server);
			if (scoreboard == null) return;
		}

		StormboundIslesMod.LOGGER.info("Refreshing all scoreboard team properties and online player assignments...");

		setupScoreboardTeamProperties();

		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			AbstractTeam currentSbTeam = scoreboard.getScoreHolderTeam(player.getGameProfile().getName());
			Team dataManagerTeam = getPlayerTeamFromDataManager(player.getUuid());

			if (currentSbTeam instanceof net.minecraft.scoreboard.Team && (dataManagerTeam == null || !currentSbTeam.getName().equals(dataManagerTeam.getName()))) {
				scoreboard.removeScoreHolderFromTeam(player.getGameProfile().getName(), (net.minecraft.scoreboard.Team) currentSbTeam);
				StormboundIslesMod.LOGGER.debug("Removed player {} from incorrect scoreboard team {} during refresh.", player.getGameProfile().getName(), currentSbTeam.getName());
			}

			addPlayerToScoreboardTeamOnJoin(player);
		}
		StormboundIslesMod.LOGGER.info("Finished refreshing scoreboard teams.");
	}

	private static Team getPlayerTeamFromDataManager(UUID playerUuid) {
		for (Team team : DataManager.getTeams().values()) {
			if (team.getMembers().contains(playerUuid)) {
				return team;
			}
		}
		return null;
	}

	private static String getDisplayNameForTeam(Team team) {
		return getTeamColor(team) + team.getName();
	}

	private static Formatting getTeamColor(Team team) {
		return switch (team.getName().toUpperCase()) {
			case "VOLCANO" -> Formatting.RED;
			case "ICE" -> Formatting.AQUA;
			case "DESERT" -> Formatting.YELLOW;
			case "MUSHROOM" -> Formatting.LIGHT_PURPLE;
			case "CRYSTAL" -> Formatting.BLUE;
			default -> Formatting.WHITE;
		};
	}
}
