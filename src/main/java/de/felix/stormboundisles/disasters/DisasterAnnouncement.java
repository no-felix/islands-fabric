package de.felix.stormboundisles.disasters;

import de.felix.stormboundisles.teams.Team;
import de.felix.stormboundisles.teams.TeamManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Utility for sending announcements to teams when disasters start/end.
 */
public class DisasterAnnouncement {
	public static void announceStart(MinecraftServer server, String teamName, String disasterName) {
		List<ServerPlayerEntity> players = getOnlinePlayersOfTeam(server, teamName);
		for (ServerPlayerEntity player : players) {
			player.sendMessage(Text.literal("§cDisaster started: §6" + disasterName), false);
		}
	}

	public static void announceEnd(MinecraftServer server, String teamName, String disasterName) {
		List<ServerPlayerEntity> players = getOnlinePlayersOfTeam(server, teamName);
		for (ServerPlayerEntity player : players) {
			player.sendMessage(Text.literal("§aDisaster ended: §6" + disasterName), false);
		}
	}

	private static List<ServerPlayerEntity> getOnlinePlayersOfTeam(MinecraftServer server, String teamName) {
		Team team = TeamManager.getInstance().getTeam(teamName);
		if (team == null) return List.of();
		return server.getPlayerManager().getPlayerList().stream()
				.filter(team::hasPlayer)
				.toList();
	}
}