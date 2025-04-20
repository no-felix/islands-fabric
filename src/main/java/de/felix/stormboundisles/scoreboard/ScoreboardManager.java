package de.felix.stormboundisles.scoreboard;

import de.felix.stormboundisles.bonus.BonusManager;
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
	private static ScoreboardManager instance;

	private static final String OBJECTIVE_NAME = "isles_points";
	private static final Text OBJECTIVE_TITLE = Text.literal("§eStormbound Isles");

	private ScoreboardManager() {
	}

	public static ScoreboardManager getInstance() {
		if (instance == null) instance = new ScoreboardManager();
		return instance;
	}

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
			ScoreHolder holder = ScoreHolder.fromName(entry.owner());
			toClear.add(holder);
		}
		for (ScoreHolder holder : toClear) {
			scoreboard.removeScore(holder, objective);
		}

		// 4) Write fresh scores for each team (sum of points and bonus!)
		for (Team team : TeamManager.getInstance().getAllTeams()) {
			String teamName = team.getName();
			int points = PointManager.getInstance().getPoints(teamName);
			int bonus = BonusManager.getInstance().getBonus(teamName);
			int total = points + bonus;

			ScoreHolder holder = ScoreHolder.fromName(teamName);
			scoreboard.getOrCreateScore(holder, objective).setScore(total);
		}
	}
}