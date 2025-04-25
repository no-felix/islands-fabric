package de.nofelix.stormboundisles.handler;

import de.nofelix.stormboundisles.StormboundIslesMod;
import de.nofelix.stormboundisles.config.ConfigManager;
import de.nofelix.stormboundisles.data.DataManager;
import de.nofelix.stormboundisles.data.Island;
import de.nofelix.stormboundisles.data.Team;
import de.nofelix.stormboundisles.game.GameManager;
import de.nofelix.stormboundisles.game.GamePhase;
import de.nofelix.stormboundisles.game.ScoreboardManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles player-related events: death penalties and boundary enforcement during the build phase.
 */
public final class PlayerEventHandler {
	private static final int DEATH_PENALTY = 10;
	private static final long WARNING_COOLDOWN_MS = 3_000L;

	private static int boundaryCheckCounter = 0;
	private static final Map<UUID, Long> lastBoundaryWarning = new HashMap<>();

	private PlayerEventHandler() {
	}

	/**
	 * Registers death and tick listeners.
	 */
	public static void register() {
		StormboundIslesMod.LOGGER.info("Registering PlayerEventHandler");
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, src) -> {
			if (entity instanceof ServerPlayerEntity player) {
				handlePlayerDeath(player);
				StormboundIslesMod.LOGGER.info(
						"Player {} died, handling death event.", player.getName().getString());
			}
		});
		ServerTickEvents.END_SERVER_TICK.register(PlayerEventHandler::onServerTick);
	}

	/**
	 * Called each server tick to enforce island boundaries in BUILD phase.
	 */
	private static void onServerTick(MinecraftServer server) {
		if (GameManager.phase != GamePhase.BUILD) return;

		if (++boundaryCheckCounter < ConfigManager.getBoundaryCheckInterval()) {
			return;
		}
		boundaryCheckCounter = 0;

		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			enforceIslandBoundary(player);
		}
	}

	/**
	 * Warns and teleports a player back if they leave their island during BUILD.
	 */
	private static void enforceIslandBoundary(ServerPlayerEntity player) {
		Optional<Team> team = DataManager.teams.values().stream()
				.filter(t -> t.members.contains(player.getUuid()))
				.findFirst();

		if (team.isEmpty() || team.get().islandId == null) return;

		Island island = DataManager.islands.get(team.get().islandId);
		if (island == null || island.zone == null) return;

		BlockPos pos = player.getBlockPos();
		if (!island.zone.containsHorizontal(pos)) {
			long now = System.currentTimeMillis();
			Long last = lastBoundaryWarning.get(player.getUuid());
			if (last == null || (now - last) > WARNING_COOLDOWN_MS) {
				player.sendMessage(
						Text.literal("§c⚠ You cannot leave your island during the build phase!"),
						true);
				lastBoundaryWarning.put(player.getUuid(), now);
			}

			if (island.spawnY >= 0) {
				ServerWorld world = player.getServerWorld();
				player.teleport(
						world,
						island.spawnX + 0.5, island.spawnY, island.spawnZ + 0.5,
						player.getYaw(), player.getPitch());
			}
		}
	}

	/**
	 * Applies point penalty on player death during BUILD or PVP phases.
	 */
	private static void handlePlayerDeath(ServerPlayerEntity player) {
		if (GameManager.phase != GamePhase.BUILD
				&& GameManager.phase != GamePhase.PVP) {
			return;
		}

		UUID id = player.getUuid();
		Optional<Team> team = DataManager.teams.values().stream()
				.filter(t -> t.members.contains(id))
				.findFirst();

		team.ifPresent(t -> {
			t.points -= DEATH_PENALTY;
			ScoreboardManager.updateTeamScore(t.name);

			String msg = "Team " + t.name + " lost " + DEATH_PENALTY +
					" points (Player death: " + player.getName().getString() + ")";
			StormboundIslesMod.LOGGER.info(msg);
			player.getServer()
					.getPlayerManager()
					.broadcast(Text.literal(msg), false);

			DataManager.saveAll();
			Island isl = DataManager.islands.get(t.islandId);
			if (isl != null && isl.spawnY >= 0) {
				player.teleport(
						player.getServerWorld(),
						isl.spawnX + 0.5, isl.spawnY, isl.spawnZ + 0.5,
						player.getYaw(), player.getPitch());
			}
		});
	}
}
