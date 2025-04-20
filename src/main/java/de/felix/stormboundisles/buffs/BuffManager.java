package de.felix.stormboundisles.buffs;

import de.felix.stormboundisles.teams.Team;
import de.felix.stormboundisles.teams.TeamManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

/**
 * Manages buffs for teams/island types and applies/removes them to players.
 */
public class BuffManager {
	private static BuffManager instance;
	private final Map<String, List<Buff>> buffsByIslandType = new HashMap<>();
	// Tracks which buffs are currently applied to each player (by buff type)
	private final Map<UUID, Set<String>> activeBuffTypes = new HashMap<>();

	private BuffManager() {
	}

	public static BuffManager getInstance() {
		if (instance == null) instance = new BuffManager();
		return instance;
	}

	public void registerBuff(String islandType, Buff buff) {
		buffsByIslandType.computeIfAbsent(islandType, k -> new ArrayList<>()).add(buff);
	}

	public List<Buff> getBuffsForIsland(String islandType) {
		return buffsByIslandType.getOrDefault(islandType, Collections.emptyList());
	}

	/**
	 * Applies all buffs for the given player if they are on their own island.
	 * Removes buffs if not on their island.
	 */
	public void handlePlayerOnIsland(ServerPlayerEntity player, boolean isOnIsland) {
		Team team = TeamManager.getInstance().getTeamOfPlayer(player.getUuidAsString());
		if (team == null) return;
		List<Buff> buffs = getBuffsForIsland(team.getIslandType());

		if (isOnIsland) {
			// Apply missing buffs
			for (Buff buff : buffs) {
				if (!hasBuff(player, buff.getType())) {
					applyBuff(player, buff);
				}
			}
		} else {
			// Remove all buffs for this island type
			for (Buff buff : buffs) {
				if (hasBuff(player, buff.getType())) {
					removeBuff(player, buff);
				}
			}
		}
	}

	public void applyBuff(ServerPlayerEntity player, Buff buff) {
		StatusEffectInstance effect = createEffectInstance(buff);
		if (effect != null) {
			player.addStatusEffect(effect);
			activeBuffTypes.computeIfAbsent(player.getUuid(), k -> new HashSet<>()).add(buff.getType());
		}
	}

	public void removeBuff(ServerPlayerEntity player, Buff buff) {
		RegistryEntry<StatusEffect> effectType = getStatusEffectByName(buff.getType());
		if (effectType != null) {
			player.removeStatusEffect(effectType);
			Set<String> set = activeBuffTypes.get(player.getUuid());
			if (set != null) {
				set.remove(buff.getType());
				if (set.isEmpty()) {
					activeBuffTypes.remove(player.getUuid());
				}
			}
		}
	}

	public boolean hasBuff(ServerPlayerEntity player, String type) {
		Set<String> set = activeBuffTypes.get(player.getUuid());
		return set != null && set.contains(type);
	}

	/**
	 * Helper to convert Buff to Minecraft StatusEffectInstance.
	 */
	private StatusEffectInstance createEffectInstance(Buff buff) {
		RegistryEntry<StatusEffect> effectType = getStatusEffectByName(buff.getType());
		if (effectType == null) return null;
		return new StatusEffectInstance(
				effectType,
				buff.getDurationTicks() == 0 ? 1000000 : buff.getDurationTicks(),
				buff.getAmplifier(),
				false,
				true,
				true
		);
	}

	/**
	 * Maps a string name to a RegistryEntry<StatusEffect>.
	 */
	private RegistryEntry<StatusEffect> getStatusEffectByName(String type) {
		// Extend this mapping as needed
		return switch (type.toLowerCase()) {
			case "regeneration" -> StatusEffects.REGENERATION;
			case "resistance" -> StatusEffects.RESISTANCE;
			case "speed" -> StatusEffects.SPEED;
			case "strength" -> StatusEffects.STRENGTH;
			default -> {
				// Fallback: try dynamic lookup (if mods/custom effects are used)
				Optional<RegistryEntry.Reference<StatusEffect>> dynamic =
						Registries.STATUS_EFFECT.streamEntries()
								.filter(e -> e.getKey().get().getValue().getPath().equalsIgnoreCase(type))
								.findFirst();
				yield dynamic.orElse(null);
			}
		};
	}
}