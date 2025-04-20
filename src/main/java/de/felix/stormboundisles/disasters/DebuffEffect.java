package de.felix.stormboundisles.disasters;

import de.felix.stormboundisles.zones.IslandZone;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.List;

/**
 * Effect: Applies a negative potion effect to all players in the zone.
 */
public class DebuffEffect implements DisasterEffect {
	private final String effectName;
	private final int amplifier;
	private final int durationTicks;

	public DebuffEffect(String effectName, int amplifier, int durationTicks) {
		this.effectName = effectName;
		this.amplifier = amplifier;
		this.durationTicks = durationTicks;
	}

	@Override
	public void apply(ServerWorld world, IslandZone zone, List<ServerPlayerEntity> players) {
		RegistryEntry<StatusEffect> effectEntry = getEffectByName(effectName);
		if (effectEntry == null) return;
		for (ServerPlayerEntity player : players) {
			player.addStatusEffect(new StatusEffectInstance(effectEntry, durationTicks, amplifier));
		}
	}

	@Override
	public void cleanup(ServerWorld world, IslandZone zone, List<ServerPlayerEntity> players) {
		// Optional: Remove effect early if desired
	}

	private RegistryEntry<StatusEffect> getEffectByName(String name) {
		return Registries.STATUS_EFFECT.streamEntries()
				.filter(e -> e.getKey().get().getValue().getPath().equalsIgnoreCase(name))
				.findFirst().orElse(null);
	}

	public String getEffectName() {
		return effectName;
	}

	public int getAmplifier() {
		return amplifier;
	}

	public int getDurationTicks() {
		return durationTicks;
	}
}