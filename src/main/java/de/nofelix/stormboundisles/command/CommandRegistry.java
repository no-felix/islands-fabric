package de.nofelix.stormboundisles.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.nofelix.stormboundisles.data.*;
import de.nofelix.stormboundisles.disaster.DisasterManager;
import de.nofelix.stormboundisles.disaster.DisasterType;
import de.nofelix.stormboundisles.game.GameManager;
import de.nofelix.stormboundisles.game.GamePhase;
import de.nofelix.stormboundisles.game.ScoreboardManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Stream;

/**
 * Registers all `/sbi` commands (island, team, points, admin, start, stop).
 */
public class CommandRegistry {
	/**
	 * Suggests existing island IDs.
	 */
	private static final SuggestionProvider<ServerCommandSource> ISLAND_ID_SUGGESTIONS = (ctx, builder) ->
			CommandSource.suggestMatching(DataManager.getIslands().keySet(), builder);

	/**
	 * Suggests existing team names.
	 */
	private static final SuggestionProvider<ServerCommandSource> TEAM_NAME_SUGGESTIONS = (ctx, builder) ->
			CommandSource.suggestMatching(DataManager.getTeams().keySet(), builder);

	/**
	 * Suggests available disaster types.
	 */
	private static final SuggestionProvider<ServerCommandSource> DISASTER_TYPE_SUGGESTIONS = (ctx, builder) ->
			CommandSource.suggestMatching(
					Stream.of(DisasterType.values()).map(Enum::name).toList(), builder);

	/**
	 * Helper for building polygon zones point by point.
	 */
	private static class PolygonBuilder {
		String islandId;
		List<BlockPos> points = new ArrayList<>();
	}

	private static final Map<UUID, PolygonBuilder> polygonBuilders = new HashMap<>();

	/**
	 * Initializes islands and teams on startup, then registers `/sbi` commands.
	 */
	public static void register() {
		// Ensure all islands and teams exist and are linked
		for (IslandType type : IslandType.values()) {
			String id = type.name().toLowerCase();
			Island island = DataManager.getIsland(id);
			if (island == null) {
				island = new Island(id, type);
				DataManager.putIsland(island);
			}
			
			String teamName = type.name();
			Team team = DataManager.getTeam(teamName);
			if (team == null) {
				team = new Team(teamName);
				DataManager.putTeam(team);
			}

			if (island.getTeamName() == null) island.setTeamName(teamName);
			if (team.getIslandId() == null) team.setIslandId(id);
		}
		DataManager.saveAll();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(
					CommandManager.literal("sbi")
							.requires(src -> src.hasPermissionLevel(2))

							// admin
							.then(CommandManager.literal("admin")
									.then(CommandManager.literal("phase")
											.then(CommandManager.argument("phase", StringArgumentType.word())
													.suggests((ctx, b) -> CommandSource.suggestMatching(
															Stream.of(GamePhase.values()).map(Enum::name).toList(), b))
													.executes(ctx -> {
														String phaseStr = StringArgumentType.getString(ctx, "phase").toUpperCase();
														try {
															GamePhase phase = GamePhase.valueOf(phaseStr);
															GameManager.setPhase(phase, ctx.getSource().getServer());
															ctx.getSource().sendFeedback(() ->
																	Text.literal("Phase set to " + phase), false);
															return 1;
														} catch (IllegalArgumentException e) {
															ctx.getSource().sendError(Text.literal("Invalid phase."));
															return 0;
														}
													})
											)
									)
									.then(CommandManager.literal("reset")
											.executes(ctx -> {
												DataManager.clearIslands();
												DataManager.clearTeams();
												DataManager.saveAll();
												ctx.getSource().sendFeedback(() ->
														Text.literal("Game data reset."), false);
												return 1;
											})
									)
							)

							// island
							.then(CommandManager.literal("island")
									.then(CommandManager.literal("disaster")
											.then(CommandManager.argument("islandId", StringArgumentType.word())
													.suggests(ISLAND_ID_SUGGESTIONS)
													.then(CommandManager.argument("type", StringArgumentType.word())
															.suggests(DISASTER_TYPE_SUGGESTIONS)
															.executes(ctx -> {
																String id = StringArgumentType.getString(ctx, "islandId");
																String typeStr = StringArgumentType.getString(ctx, "type").toUpperCase();
																try {
																	DisasterType type = DisasterType.valueOf(typeStr);
																	DisasterManager.triggerDisaster(ctx.getSource().getServer(), id, type);
																	ctx.getSource().sendFeedback(() ->
																			Text.literal("Disaster triggered: " + type + " on " + id), false);
																	return 1;
																} catch (IllegalArgumentException e) {
																	ctx.getSource().sendError(Text.literal("Invalid disaster type."));
																	return 0;
																}
															})
													)
											)
									)
									.then(CommandManager.literal("list")
											.executes(ctx -> {
												StringBuilder sb = new StringBuilder("Islands:\n");
												for (Island isl : DataManager.getIslands().values()) {
													String zoneInfo = switch (isl.getZone()) {
														case null -> "Not set";
														case PolygonZone pz ->
																"Polygon (" + pz.getPoints().size() + " points)";
														default -> "Unknown";
													};
													sb.append(isl.getId())
															.append(" (").append(isl.getType()).append(")")
															.append(" | Team: ").append(isl.getTeamName())
															.append(" | Zone: ").append(zoneInfo)
															.append("\n");
												}
												ctx.getSource().sendFeedback(() ->
														Text.literal(sb.toString()), false);
												return 1;
											})
									)
									.then(CommandManager.literal("polyzone")
											.then(CommandManager.literal("start")
													.then(CommandManager.argument("islandId", StringArgumentType.word())
															.suggests(ISLAND_ID_SUGGESTIONS)
															.executes(ctx -> {
																UUID uid = ctx.getSource().getPlayer().getUuid();
																PolygonBuilder pb = new PolygonBuilder();
																pb.islandId = StringArgumentType.getString(ctx, "islandId");
																polygonBuilders.put(uid, pb);
																ctx.getSource().sendFeedback(() ->
																		Text.literal("Polygon definition started for island " + pb.islandId), false);
																return 1;
															})
													)
											)
											.then(CommandManager.literal("addpoint")
													.executes(ctx -> {
														ServerPlayerEntity player = ctx.getSource().getPlayer();
														UUID uid = player.getUuid();
														PolygonBuilder pb = polygonBuilders.get(uid);
														if (pb == null) {
															ctx.getSource().sendError(Text.literal("Use /sbi island polyzone start <islandId> first."));
															return 0;
														}
														pb.points.add(player.getBlockPos());
														ctx.getSource().sendFeedback(() ->
																Text.literal("Added point " + player.getBlockPos() + " to polygon"), false);
														return 1;
													})
											)
											.then(CommandManager.literal("finish")
													.executes(ctx -> {
														UUID uid = ctx.getSource().getPlayer().getUuid();
														PolygonBuilder pb = polygonBuilders.remove(uid);
														if (pb == null || pb.points.size() < 3) {
															ctx.getSource().sendError(Text.literal("At least 3 points are required."));
															return 0;
														}
														Island isl = DataManager.getIsland(pb.islandId);
														if (isl == null) {
															ctx.getSource().sendError(Text.literal("Island does not exist."));
															return 0;
														}
														isl.setZone(new PolygonZone(pb.points));
														DataManager.saveAll();
														ctx.getSource().sendFeedback(() ->
																Text.literal("Polygon zone set for island " + pb.islandId), false);
														return 1;
													})
											)
									)
									.then(CommandManager.literal("setspawn")
											.then(CommandManager.argument("islandId", StringArgumentType.word())
													.suggests(ISLAND_ID_SUGGESTIONS)
													.executes(ctx -> {
														ServerPlayerEntity player = ctx.getSource().getPlayer();
														Island isl = DataManager.getIsland(StringArgumentType.getString(ctx, "islandId"));
														if (isl == null) {
															ctx.getSource().sendError(Text.literal("Island does not exist."));
															return 0;
														}
														BlockPos pos = player.getBlockPos();
														isl.setSpawnPoint(pos.getX(), pos.getY(), pos.getZ());
														DataManager.saveAll();
														ctx.getSource().sendFeedback(() ->
																Text.literal("Spawn for island " + isl.getId() + " set to " + pos), false);
														return 1;
													})
											)
									)
									.then(CommandManager.literal("zone1")
											.then(CommandManager.argument("islandId", StringArgumentType.word())
													.suggests(ISLAND_ID_SUGGESTIONS)
													.executes(ctx -> {
														ServerPlayerEntity player = ctx.getSource().getPlayer();
														UUID uid = player.getUuid();
														
														// Start a polygon builder instead of using zone corners
														PolygonBuilder pb = new PolygonBuilder();
														pb.islandId = StringArgumentType.getString(ctx, "islandId");
														pb.points.add(player.getBlockPos()); // Add first corner
														polygonBuilders.put(uid, pb);
														
														ctx.getSource().sendFeedback(() ->
																Text.literal("First corner set at your position. Use /sbi island zone2 to add the second corner."), false);
														return 1;
													})
											)
									)
									.then(CommandManager.literal("zone2")
											.then(CommandManager.argument("islandId", StringArgumentType.word())
													.suggests(ISLAND_ID_SUGGESTIONS)
													.executes(ctx -> {
														ServerPlayerEntity player = ctx.getSource().getPlayer();
														String id = StringArgumentType.getString(ctx, "islandId");
														Island isl = DataManager.getIsland(id);
														UUID uid = player.getUuid();
														
														PolygonBuilder pb = polygonBuilders.get(uid);
														if (isl == null || pb == null || !pb.islandId.equals(id)) {
															ctx.getSource().sendError(Text.literal(
																	isl == null ? "Island does not exist." : "First corner not set. Use /sbi island zone1 to set it."));
															return 0;
														}
														
														// Add remaining points to form a rectangle
														BlockPos firstPos = pb.points.get(0);
														BlockPos secondPos = player.getBlockPos();
														
														// Use the new createRectangle method which handles coordinate ordering properly
														isl.setZone(PolygonZone.createRectangle(firstPos, secondPos));
														polygonBuilders.remove(uid);
														DataManager.saveAll();
														ctx.getSource().sendFeedback(() ->
																Text.literal("Rectangular zone for island " + id + " set."), false);
														return 1;
													})
											)
									)
							)

							// points
							.then(CommandManager.literal("points")
									.then(CommandManager.literal("add")
											.then(CommandManager.argument("team", StringArgumentType.word())
													.suggests(TEAM_NAME_SUGGESTIONS)
													.then(CommandManager.argument("amount", IntegerArgumentType.integer())
															.then(CommandManager.argument("reason", StringArgumentType.greedyString())
																	.executes(ctx -> {
																		Team t = DataManager.getTeam(StringArgumentType.getString(ctx, "team"));
																		if (t == null) {
																			ctx.getSource().sendError(Text.literal("Team does not exist."));
																			return 0;
																		}
																		int amt = IntegerArgumentType.getInteger(ctx, "amount");
																		t.addPoints(amt);
																		DataManager.saveAll();
																		ScoreboardManager.updateTeamScore(t.getName());
																		ctx.getSource().sendFeedback(() ->
																						Text.literal("Added " + amt + " points to " + t.getName() +
																								" (" + StringArgumentType.getString(ctx, "reason") + ")"),
																				false);
																		return 1;
																	})
															)
															.executes(ctx -> {
																Team t = DataManager.getTeam(StringArgumentType.getString(ctx, "team"));
																if (t == null) {
																	ctx.getSource().sendError(Text.literal("Team does not exist."));
																	return 0;
																}
																int amt = IntegerArgumentType.getInteger(ctx, "amount");
																t.addPoints(amt);
																DataManager.saveAll();
																ScoreboardManager.updateTeamScore(t.getName());
																ctx.getSource().sendFeedback(() ->
																		Text.literal("Added " + amt + " points to " + t.getName()), false);
																return 1;
															})
													)
											)
									)
									.then(CommandManager.literal("remove")
											.then(CommandManager.argument("team", StringArgumentType.word())
													.suggests(TEAM_NAME_SUGGESTIONS)
													.then(CommandManager.argument("amount", IntegerArgumentType.integer())
															.then(CommandManager.argument("reason", StringArgumentType.greedyString())
																	.executes(ctx -> {
																		Team t = DataManager.getTeam(StringArgumentType.getString(ctx, "team"));
																		if (t == null) {
																			ctx.getSource().sendError(Text.literal("Team does not exist."));
																			return 0;
																		}
																		int amt = IntegerArgumentType.getInteger(ctx, "amount");
																		t.addPoints(-amt); // Use addPoints with negative value
																		DataManager.saveAll();
																		ScoreboardManager.updateTeamScore(t.getName());
																		ctx.getSource().sendFeedback(() ->
																						Text.literal("Removed " + amt + " points from " + t.getName() +
																								" (" + StringArgumentType.getString(ctx, "reason") + ")"),
																				false);
																		return 1;
																	})
															)
															.executes(ctx -> {
																Team t = DataManager.getTeam(StringArgumentType.getString(ctx, "team"));
																if (t == null) {
																	ctx.getSource().sendError(Text.literal("Team does not exist."));
																	return 0;
																}
																int amt = IntegerArgumentType.getInteger(ctx, "amount");
																t.addPoints(-amt); // Use addPoints with negative value
																DataManager.saveAll();
																ScoreboardManager.updateTeamScore(t.getName());
																ctx.getSource().sendFeedback(() ->
																		Text.literal("Removed " + amt + " points from " + t.getName()), false);
																return 1;
															})
													)
											)
									)
							)

							// start
							.then(CommandManager.literal("start")
									.executes(ctx -> {
										GameManager.startCountdown(ctx.getSource().getServer());
										ctx.getSource().sendFeedback(() ->
												Text.literal("Countdown to game start initiated."), false);
										return 1;
									})
							)

							// stop
							.then(CommandManager.literal("stop")
									.executes(ctx -> {
										GameManager.stopGame(ctx.getSource().getServer());
										ctx.getSource().sendFeedback(() ->
												Text.literal("Game stopped."), false);
										return 1;
									})
							)

							// team
							.then(CommandManager.literal("team")
									.then(CommandManager.literal("assign")
											.then(CommandManager.argument("teamName", StringArgumentType.word())
													.suggests(TEAM_NAME_SUGGESTIONS)
													.then(CommandManager.argument("player", StringArgumentType.word())
															.suggests((ctx, b) -> CommandSource.suggestMatching(
																	ctx.getSource().getServer().getPlayerManager().getPlayerList()
																			.stream().map(p -> p.getName().getString()).toList(), b))
															.executes(ctx -> {
																String teamName = StringArgumentType.getString(ctx, "teamName");
																Team team = DataManager.getTeam(teamName);
																if (team == null) {
																	ctx.getSource().sendError(Text.literal("Team does not exist."));
																	return 0;
																}
																ServerPlayerEntity target = ctx.getSource().getServer()
																		.getPlayerManager().getPlayer(StringArgumentType.getString(ctx, "player"));
																if (target == null) {
																	ctx.getSource().sendError(Text.literal("Player not found."));
																	return 0;
																}
																
																// Remove player from all teams
																for (Team t : DataManager.getTeams().values()) {
																	t.removeMember(target.getUuid());
																}
																
																// Add player to specified team
																team.addMember(target.getUuid());
																DataManager.saveAll();
																ScoreboardManager.updateAllTeams(ctx.getSource().getServer());
																ctx.getSource().sendFeedback(() ->
																		Text.literal("Assigned " + target.getName().getString() +
																				" to team " + teamName), false);
																return 1;
															})
													)
											)
									)
									.then(CommandManager.literal("remove")
											.then(CommandManager.argument("player", StringArgumentType.word())
													.suggests((ctx, b) -> CommandSource.suggestMatching(
															ctx.getSource().getServer().getPlayerManager().getPlayerList()
																	.stream().map(p -> p.getName().getString()).toList(), b))
													.executes(ctx -> {
														ServerPlayerEntity target = ctx.getSource().getServer()
																.getPlayerManager().getPlayer(StringArgumentType.getString(ctx, "player"));
														if (target == null) {
															ctx.getSource().sendError(Text.literal("Player not found."));
															return 0;
														}
														
														// Remove player from all teams
														for (Team t : DataManager.getTeams().values()) {
															t.removeMember(target.getUuid());
														}
														
														DataManager.saveAll();
														ScoreboardManager.updateAllTeams(ctx.getSource().getServer());
														ctx.getSource().sendFeedback(() ->
																Text.literal("Removed " + target.getName().getString() +
																		" from all teams."), false);
														return 1;
													})
											)
									)
							)
			);
		});
	}
}
