package de.felix.stormboundisles.zones;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.*;

/**
 * Helper class for admins to interactively define a zone.
 */
public class ZoneSelectionSession {
	private static final Map<UUID, ZoneSelectionSession> activeSessions = new HashMap<>();
	private final String teamName;
	private final List<BlockPos> selectedPoints = new ArrayList<>();

	public ZoneSelectionSession(String teamName) {
		this.teamName = teamName;
	}

	public static void startFor(ServerPlayerEntity player, String teamName) {
		activeSessions.put(player.getUuid(), new ZoneSelectionSession(teamName));
	}

	public static void addPoint(ServerPlayerEntity player, BlockPos pos) {
		ZoneSelectionSession session = activeSessions.get(player.getUuid());
		if (session != null) {
			session.selectedPoints.add(pos);
		}
	}

	public static void finish(ServerPlayerEntity player) {
		ZoneSelectionSession session = activeSessions.remove(player.getUuid());
		if (session != null && session.selectedPoints.size() >= 3) {
			IslandZone zone = new IslandZone(session.teamName, session.selectedPoints, null);
			ZoneManager.getInstance().addZone(zone);
		}
	}

	public static boolean isActive(ServerPlayerEntity player) {
		return activeSessions.containsKey(player.getUuid());
	}
}