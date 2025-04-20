package de.felix.stormboundisles.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.felix.stormboundisles.bonus.BonusManager;
import de.felix.stormboundisles.buffs.Buff;
import de.felix.stormboundisles.buffs.BuffManager;
import de.felix.stormboundisles.disasters.*;
import de.felix.stormboundisles.points.PointManager;
import de.felix.stormboundisles.teams.Team;
import de.felix.stormboundisles.teams.TeamManager;
import de.felix.stormboundisles.zones.IslandZone;
import de.felix.stormboundisles.zones.ZoneManager;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and saves mod configuration (teams, zones, disasters, buffs, points).
 */
public class ConfigLoader {
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private final Type mapType = new TypeToken<HashMap<String, Object>>() {
	}.getType();

	public void loadConfig(File file) throws IOException {
		try (FileReader reader = new FileReader(file)) {
			Map<String, Object> config = gson.fromJson(reader, mapType);

			// Teams
			List<Map<String, String>> teams = (List<Map<String, String>>) config.get("teams");
			if (teams != null) {
				for (Map<String, String> entry : teams) {
					TeamManager.getInstance().addTeam(new Team(
							entry.get("name"),
							entry.get("color"),
							entry.get("islandType")
					));
				}
			}

			// Zones
			List<Map<String, Object>> zones = (List<Map<String, Object>>) config.get("zones");
			if (zones != null) {
				for (Map<String, Object> entry : zones) {
					String teamName = (String) entry.get("teamName");
					String islandType = (String) entry.get("islandType");
					List<Map<String, Number>> points = (List<Map<String, Number>>) entry.get("polygonPoints");
					List<BlockPos> posList = points.stream()
							.map(pos -> new BlockPos(
									pos.get("x").intValue(),
									pos.get("y").intValue(),
									pos.get("z").intValue()))
							.toList();
					ZoneManager.getInstance().addZone(new IslandZone(teamName, posList, islandType));
				}
			}

			// Points
			Map<String, Double> points = (Map<String, Double>) config.get("points");
			if (points != null) {
				for (Map.Entry<String, Double> entry : points.entrySet()) {
					PointManager.getInstance().setPoints(entry.getKey(), entry.getValue().intValue());
				}
			}

			// Bonus
			Map<String, Double> bonus = (Map<String, Double>) config.get("bonus");
			if (bonus != null) {
				for (Map.Entry<String, Double> entry : bonus.entrySet()) {
					BonusManager.getInstance().setBonus(entry.getKey(), entry.getValue().intValue());
				}
			}

			// Buffs
			Map<String, List<Map<String, Object>>> buffs = (Map<String, List<Map<String, Object>>>) config.get("buffs");
			if (buffs != null) {
				for (Map.Entry<String, List<Map<String, Object>>> entry : buffs.entrySet()) {
					String islandType = entry.getKey();
					for (Map<String, Object> buffMap : entry.getValue()) {
						String type = (String) buffMap.get("type");
						int amplifier = ((Number) buffMap.get("amplifier")).intValue();
						int durationTicks = ((Number) buffMap.get("durationTicks")).intValue();
						BuffManager.getInstance().registerBuff(islandType, new Buff(type, amplifier, durationTicks, new String[]{islandType}));
					}
				}
			}

			// Disasters
			Map<String, List<Map<String, Object>>> disasters = (Map<String, List<Map<String, Object>>>) config.get("disasters");
			if (disasters != null) {
				for (Map.Entry<String, List<Map<String, Object>>> entry : disasters.entrySet()) {
					String islandType = entry.getKey();
					for (Map<String, Object> disasterMap : entry.getValue()) {
						String name = (String) disasterMap.get("name");
						double chancePerHour = ((Number) disasterMap.get("chancePerHour")).doubleValue();
						int durationSeconds = ((Number) disasterMap.get("durationSeconds")).intValue();
						List<Map<String, Object>> effectsList = (List<Map<String, Object>>) disasterMap.get("effects");
						List<DisasterEffect> effects = new ArrayList<>();
						for (Map<String, Object> eff : effectsList) {
							String effectType = (String) eff.get("effectType");
							switch (effectType) {
								case "FireRainEffect" ->
										effects.add(new FireRainEffect(((Number) eff.get("numFireballs")).intValue()));
								case "LightningEffect" ->
										effects.add(new LightningEffect(((Number) eff.get("strikes")).intValue()));
								case "MobSpawnEffect" -> {
									String mobId = (String) eff.get("mobId");
									int count = ((Number) eff.get("count")).intValue();
									EntityType<?> mobType = EntityType.get(mobId).orElse(EntityType.ZOMBIE);
									effects.add(new MobSpawnEffect(mobType, count));
								}
								case "DebuffEffect" -> {
									String effectName = (String) eff.get("effectName");
									int amplifier = ((Number) eff.get("amplifier")).intValue();
									int durationTicks = ((Number) eff.get("durationTicks")).intValue();
									effects.add(new DebuffEffect(effectName, amplifier, durationTicks));
								}
								default -> {
								}
							}
						}
						Disaster disaster = new Disaster(name, chancePerHour, durationSeconds, effects);
						DisasterManager.getInstance().registerDisaster(islandType, disaster);
					}
				}
			}
		}
	}

	public void saveConfig(File file) throws IOException {
		Map<String, Object> config = new HashMap<>();
		// Teams
		List<Team> teams = List.copyOf(TeamManager.getInstance().getAllTeams());
		config.put("teams", teams.stream().map(team -> {
			Map<String, String> entry = new HashMap<>();
			entry.put("name", team.getName());
			entry.put("color", team.getColorCode());
			entry.put("islandType", team.getIslandType());
			return entry;
		}).toList());

		// Zonen
		config.put("zones", ZoneManager.getInstance().getAllZones().stream().map(zone -> {
			Map<String, Object> entry = new HashMap<>();
			entry.put("teamName", zone.getTeamName());
			entry.put("islandType", zone.getIslandType());
			entry.put("polygonPoints", zone.getPolygonPoints().stream().map(pos -> Map.of(
					"x", pos.getX(), "y", pos.getY(), "z", pos.getZ()
			)).toList());
			return entry;
		}).toList());

		// Punkte
		Map<String, Integer> points = new HashMap<>();
		for (Team t : TeamManager.getInstance().getAllTeams())
			points.put(t.getName(), PointManager.getInstance().getPoints(t.getName()));
		config.put("points", points);

		// Bonus
		Map<String, Integer> bonus = new HashMap<>();
		for (Team t : TeamManager.getInstance().getAllTeams())
			bonus.put(t.getName(), BonusManager.getInstance().getBonus(t.getName()));
		config.put("bonus", bonus);

		// Buffs
		Map<String, List<Map<String, Object>>> buffs = new HashMap<>();
		for (String islandType : BuffManager.getInstance().getIslandTypes()) {
			List<Buff> buffList = BuffManager.getInstance().getBuffsForIsland(islandType);
			buffs.put(islandType, buffList.stream().map(b -> {
				Map<String, Object> m = new HashMap<>();
				m.put("type", b.getType());
				m.put("amplifier", b.getAmplifier());
				m.put("durationTicks", b.getDurationTicks());
				return m;
			}).toList());
		}
		config.put("buffs", buffs);

		// Disasters
		Map<String, List<Map<String, Object>>> disasters = new HashMap<>();
		for (String islandType : DisasterManager.getInstance().getIslandTypes()) {
			List<Disaster> disasterList = DisasterManager.getInstance().getDisastersForIsland(islandType);
			disasters.put(islandType, disasterList.stream().map(d -> {
				Map<String, Object> m = new HashMap<>();
				m.put("name", d.getName());
				m.put("chancePerHour", d.getChancePerHour());
				m.put("durationSeconds", d.getDurationSeconds());
				m.put("effects", d.getEffects().stream().map(e -> {
					Map<String, Object> eff = new HashMap<>();
					if (e instanceof FireRainEffect fr) {
						eff.put("effectType", "FireRainEffect");
						eff.put("numFireballs", fr.getNumFireballs());
					} else if (e instanceof LightningEffect le) {
						eff.put("effectType", "LightningEffect");
						eff.put("strikes", le.getStrikes());
					} else if (e instanceof MobSpawnEffect ms) {
						eff.put("effectType", "MobSpawnEffect");
						eff.put("mobId", EntityType.getId(ms.getMobType()).toString());
						eff.put("count", ms.getCount());
					} else if (e instanceof DebuffEffect db) {
						eff.put("effectType", "DebuffEffect");
						eff.put("effectName", db.getEffectName());
						eff.put("amplifier", db.getAmplifier());
						eff.put("durationTicks", db.getDurationTicks());
					}
					return eff;
				}).toList());
				return m;
			}).toList());
		}
		config.put("disasters", disasters);

		try (FileWriter writer = new FileWriter(file)) {
			gson.toJson(config, writer);
		}
	}
}