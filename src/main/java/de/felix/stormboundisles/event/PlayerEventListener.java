package de.felix.stormboundisles.event;

import de.felix.stormboundisles.buffs.BuffManager;
import de.felix.stormboundisles.points.PointManager;
import de.felix.stormboundisles.teams.Team;
import de.felix.stormboundisles.teams.TeamManager;
import de.felix.stormboundisles.zones.IslandZone;
import de.felix.stormboundisles.zones.ZoneManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Handles player-related events (movement, death, etc.).
 */
public class PlayerEventListener {

	public static void onPlayerMove(ServerPlayerEntity player, BlockPos newPos) {
		Team team = TeamManager.getInstance().getTeamOfPlayer(player.getUuidAsString());
		if (team == null) return;
		IslandZone zone = ZoneManager.getInstance().getZoneForTeam(team.getName());
		if (zone == null) return;

		boolean inside = zone.isInside(newPos);
		BuffManager.getInstance().handlePlayerOnIsland(player, inside);
	}

	public static void onPlayerDeath(ServerPlayerEntity player) {
		// Apply death penalty and update scoreboard
		PointManager.getInstance().applyDeathPenalty(player);
	}
}
