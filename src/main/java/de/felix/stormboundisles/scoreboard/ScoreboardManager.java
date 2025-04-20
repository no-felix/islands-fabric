package de.felix.stormboundisles.scoreboard;

import de.felix.stormboundisles.points.PointManager;
import de.felix.stormboundisles.teams.Team;
import de.felix.stormboundisles.teams.TeamManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Handles sidebar scoreboard updates for all players.
 * (Stub - fill in with Minecraft scoreboard API or custom rendering)
 */
public class ScoreboardManager {
	private static ScoreboardManager instance;

	private ScoreboardManager() {
	}

	public static ScoreboardManager getInstance() {
		if (instance == null) instance = new ScoreboardManager();
		return instance;
	}

	public void updateAll(MinecraftServer server) {
		// TODO: Use Minecraft's Scoreboard API for a sidebar scoreboard
		// For now, just send a chat message as a placeholder
		StringBuilder sb = new StringBuilder();
		sb.append("§6Team Scores:\n");
		for (Team team : TeamManager.getInstance().getAllTeams()) {
			int pts = PointManager.getInstance().getPoints(team.getName());
			sb.append("§e").append(team.getName()).append(": §b").append(pts).append("\n");
		}
		Text msg = Text.literal(sb.toString());

		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			player.sendMessage(msg, false);
		}
	}
}