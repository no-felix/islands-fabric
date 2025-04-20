package de.felix.stormboundisles.event;

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

	// This method would be called from a Fabric event hook (e.g., PlayerMoveCallback)
	public static void onPlayerMove(ServerPlayerEntity player, BlockPos newPos) {
		Team team = TeamManager.getInstance().getTeamOfPlayer(player.getUuidAsString());
		if (team == null) return;
		IslandZone zone = ZoneManager.getInstance().getZoneForTeam(team.getName());
		if (zone == null) return;

		boolean inside = zone.isInside(newPos);
		// TODO: Call BuffManager to apply/remove buffs depending on inside/outside state
	}

	// This method would be called from a Fabric event hook (e.g., PlayerDeathCallback)
	public static void onPlayerDeath(ServerPlayerEntity player) {
		Team team = TeamManager.getInstance().getTeamOfPlayer(player.getUuidAsString());
		if (team == null) return;
		// TODO: Apply death penalty using PointManager
	}
}
