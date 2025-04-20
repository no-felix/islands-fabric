package de.felix.stormboundisles.zones;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for admins to interactively define a zone.
 */
public class ZoneSelectionSession {
	private final ServerPlayerEntity admin;
	private final String teamName;
	private final List<BlockPos> points = new ArrayList<>();

	public ZoneSelectionSession(ServerPlayerEntity admin, String teamName) {
		this.admin = admin;
		this.teamName = teamName;
	}

	public void addPoint(BlockPos pos) {
		points.add(pos);
		// TODO: Feedback: "Point added!"
	}

	public List<BlockPos> getPoints() {
		return points;
	}

	public IslandZone toIslandZone(String islandType) {
		return new IslandZone(teamName, new ArrayList<>(points), islandType);
	}

	public ServerPlayerEntity getAdmin() {
		return admin;
	}

	public String getTeamName() {
		return teamName;
	}
}