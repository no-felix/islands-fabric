package de.felix.stormboundisles.scoreboard;

import de.felix.stormboundisles.points.PointManager;
import de.felix.stormboundisles.teams.Team;
import de.felix.stormboundisles.teams.TeamManager;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles sidebar scoreboard updates for all players.
 */
public class ScoreboardManager {
	private static final ScoreboardManager INSTANCE = new ScoreboardManager();
	private static final String OBJECTIVE_NAME = "isles_points";
	private static final Text OBJECTIVE_TITLE = Text.literal("§eStormbound Isles");

	private ScoreboardManager() {
	}

	public static ScoreboardManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Updates the sidebar scoreboard for all players.
	 *
	 * @param server the running Minecraft server
	 */
	public void updateAll(MinecraftServer server) {
		Scoreboard scoreboard = server.getScoreboard();

		// 1) Get existing objective or create a new DUMMY one with integer render
		ScoreboardObjective objective = scoreboard.getNullableObjective(OBJECTIVE_NAME);
		if (objective == null) {
			objective = scoreboard.addObjective(
					OBJECTIVE_NAME,
					ScoreboardCriterion.DUMMY,
					OBJECTIVE_TITLE,
					ScoreboardCriterion.RenderType.INTEGER,
					false,    // displayAutoUpdate
					null      // numberFormat
			);
		}

		// 2) Show it in the sidebar slot
		scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, objective);

		// 3) Remove all old scores for this objective
		List<ScoreHolder> toClear = new ArrayList<>();
		for (ScoreboardEntry entry : scoreboard.getScoreboardEntries(objective)) {
			// Extract the raw holder name and wrap it into a ScoreHolder
			ScoreHolder holder = ScoreHolder.fromName(entry.owner());
			toClear.add(holder);
		}
		for (ScoreHolder holder : toClear) {
			scoreboard.removeScore(holder, objective);
		}

		// 4) Write fresh scores for each team
		for (Team team : TeamManager.getInstance().getAllTeams()) {
			String teamName = team.getName();
			int points = PointManager.getInstance().getPoints(teamName);

			ScoreHolder holder = ScoreHolder.fromName(teamName);
			// getOrCreateScore returns a ScoreAccess which lets us set an exact value
			scoreboard.getOrCreateScore(holder, objective).setScore(points);
		}
	}
}