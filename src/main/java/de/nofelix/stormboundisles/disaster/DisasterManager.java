package de.nofelix.stormboundisles.disaster;

import de.nofelix.stormboundisles.StormboundIslesMod;
import de.nofelix.stormboundisles.config.ConfigManager;
import de.nofelix.stormboundisles.data.DataManager;
import de.nofelix.stormboundisles.data.Island;
import de.nofelix.stormboundisles.data.IslandType;
import de.nofelix.stormboundisles.game.ActionbarNotifier;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages the triggering and effects of random disasters on islands based on configured intervals.
 * Disasters are chosen based on island type and apply effects to players within the island's zone.
 */
public class DisasterManager {
	/** Maps island types to the possible disasters that can occur on them. */
	private static final Map<IslandType, DisasterType[]> ISLAND_DISASTER_TYPES = Map.of(
			IslandType.VOLCANO, new DisasterType[]{DisasterType.METEOR},
			IslandType.ICE, new DisasterType[]{DisasterType.BLIZZARD},
			IslandType.DESERT, new DisasterType[]{DisasterType.SANDSTORM},
			IslandType.MUSHROOM, new DisasterType[]{DisasterType.SPORE},
			IslandType.CRYSTAL, new DisasterType[]{DisasterType.CRYSTAL_STORM}
	);
	
	/** Maps disaster types to their effect application strategy */
	private static final Map<DisasterType, DisasterEffect> DISASTER_EFFECTS = Map.of(
	        DisasterType.METEOR, (player, server) -> {
	            player.damage(server.getOverworld().getDamageSources().generic(), 
	                    ConfigManager.getDisasterMeteorDamage());
	        },
	        DisasterType.BLIZZARD, (player, server) -> {
	            player.setFrozenTicks(player.getFrozenTicks() + 
	                    ConfigManager.getDisasterBlizzardFreezeTicks());
	        },
	        DisasterType.SANDSTORM, (player, server) -> {
	            player.addStatusEffect(new StatusEffectInstance(
	                    StatusEffects.BLINDNESS, 
	                    ConfigManager.getDisasterEffectDurationTicks(), 0));
	        },
	        DisasterType.SPORE, (player, server) -> {
	            player.addStatusEffect(new StatusEffectInstance(
	                    StatusEffects.POISON, 
	                    ConfigManager.getDisasterEffectDurationTicks(), 0));
	        },
	        DisasterType.CRYSTAL_STORM, (player, server) -> {
	            player.addStatusEffect(new StatusEffectInstance(
	                    StatusEffects.LEVITATION, 
	                    ConfigManager.getDisasterEffectDurationTicks(), 0));
	        }
	);

	/** Counter for server ticks to determine when to trigger the next disaster check. */
	private static int tickCounter = 0;
	
	/** Set containing keys representing currently active disasters (format: "islandId:DisasterType"). */
	private static final Set<String> activeDisasters = new HashSet<>();
	
	/** Map tracking when each disaster expires, to avoid Timer threads */
	private static final Object2LongMap<String> disasterExpirationTimes = new Object2LongOpenHashMap<>();

	/**
	 * Registers the server tick event listener used for disaster management.
	 */
	public static void register() {
		StormboundIslesMod.LOGGER.info("Registering DisasterManager");
		ServerTickEvents.END_SERVER_TICK.register(DisasterManager::onServerTick);
	}

	/**
	 * Triggers a specific disaster on a given island.
	 * Applies effects to players currently within the island's zone and broadcasts a server-wide message.
	 * Prevents triggering the same disaster type if it's already active on the island.
	 * Disasters are automatically removed after their duration by the periodic checkExpiredDisasters mechanism
	 * which runs during server ticks.
	 *
	 * @param server   The Minecraft server instance.
	 * @param islandId The ID of the island where the disaster occurs.
	 * @param type     The type of disaster to trigger.
	 * @return True if disaster was triggered, false if the island doesn't exist or already has this disaster.
	 */
	public static boolean triggerDisaster(MinecraftServer server, String islandId, DisasterType type) {
		Island island = DataManager.getIsland(islandId);
		// Ensure island and its zone exist
		if (island == null || island.getZone() == null) return false;

		String disasterKey = islandId + ":" + type;
		// Prevent duplicate active disasters of the same type on the same island
		if (activeDisasters.contains(disasterKey)) return false;
		
		// Register the disaster as active with its expiration time
		long expirationTime = System.currentTimeMillis() + 
		        ((long) ConfigManager.getDisasterCooldownTicks() * 50L);
		
		activeDisasters.add(disasterKey);
		disasterExpirationTimes.put(disasterKey, expirationTime);
		
		// Log disaster activation
		StormboundIslesMod.LOGGER.info("Triggering disaster: {} on island: {}", type, islandId);

		// Broadcast message to all players
		broadcastDisasterMessage(server, islandId, type, false);

		// Apply effects and action bar notifications to players on the island
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			if (island.getZone().contains(player.getBlockPos())) {
				notifyPlayerOfDisaster(player, type);
				applyDisasterEffect(player, type, server);
			}
		}
		
		return true;
	}
	
	/**
	 * Cancels an active disaster on a specific island.
	 * This method removes all active disasters associated with the given island ID.
	 *
	 * @param server The Minecraft server instance.
	 * @param islandId The ID of the island where disasters should be cancelled.
	 * @return True if any disasters were cancelled, false if no active disasters were found.
	 */
	public static boolean cancelActiveDisaster(MinecraftServer server, String islandId) {
		Island island = DataManager.getIsland(islandId);
		if (island == null) return false;
		
		// Find and remove all disasters for this island
		Set<String> disastersToRemove = new HashSet<>();
		for (String key : activeDisasters) {
			if (key.startsWith(islandId + ":")) {
				disastersToRemove.add(key);
			}
		}
		
		if (disastersToRemove.isEmpty()) {
			return false; // No active disasters found
		}
		
		// Remove all found disaster keys
		for (String key : disastersToRemove) {
			activeDisasters.remove(key);
			disasterExpirationTimes.removeLong(key);
		}
		
		// Notify players on the island
		if (island.getZone() != null) {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				if (island.getZone().contains(player.getBlockPos())) {
					ActionbarNotifier.send(player, "§aDisaster on " + islandId + " has subsided!");
				}
			}
		}
		
		// Broadcast message
		broadcastDisasterMessage(server, islandId, null, true);
		
		StormboundIslesMod.LOGGER.info("Cancelled {} disasters on island: {}", 
		        disastersToRemove.size(), islandId);
		
		return true;
	}
	
	/**
	 * Sends a disaster alert or cancellation message to all players.
	 * 
	 * @param server The server instance
	 * @param islandId The affected island
	 * @param type The disaster type (null if cancellation)
	 * @param isCancellation Whether this is a cancellation message
	 */
	private static void broadcastDisasterMessage(MinecraftServer server, String islandId, 
	        DisasterType type, boolean isCancellation) {
	    if (isCancellation) {
	        server.getPlayerManager().broadcast(
	                Text.literal("The disaster on " + islandId + " has been cancelled.")
	                        .formatted(Formatting.GREEN), false);
	    } else {
	        server.getPlayerManager().broadcast(
	                Text.literal("Disaster on " + islandId + ": " + type.name())
	                        .formatted(Formatting.RED), false);
	    }
	}
	
	/**
	 * Notifies a player about an active disaster via action bar.
	 * 
	 * @param player The player to notify
	 * @param type The type of disaster
	 */
	private static void notifyPlayerOfDisaster(ServerPlayerEntity player, DisasterType type) {
	    ActionbarNotifier.send(player, "§cDisaster: " + type.name() + "!");
	}

	/**
	 * Applies the specific effect of a disaster to a player.
	 * Effects include damage, status effects (freezing, blindness, poison, levitation).
	 *
	 * @param player The player to apply the effect to.
	 * @param type The type of disaster.
	 * @param server The Minecraft server instance (needed for damage sources).
	 */
	private static void applyDisasterEffect(ServerPlayerEntity player, DisasterType type, MinecraftServer server) {
	    DisasterEffect effect = DISASTER_EFFECTS.get(type);
	    if (effect != null) {
	        effect.apply(player, server);
	    }
	}
	
	/**
	 * A functional interface defining how disaster effects are applied to players.
	 */
	@FunctionalInterface
	private interface DisasterEffect {
	    /**
	     * Apply a disaster effect to a player.
	     * 
	     * @param player The player to affect
	     * @param server The server instance
	     */
	    void apply(ServerPlayerEntity player, MinecraftServer server);
	}

	/**
	 * Called every server tick via the registered event listener.
	 * Performs two main functions:
	 * 1. Checks and removes expired disasters
	 * 2. Periodically triggers random disasters based on configured interval
	 *
	 * @param server The Minecraft server instance.
	 */
	private static void onServerTick(MinecraftServer server) {
	    // Check for expired disasters
	    checkExpiredDisasters();
	    
	    // Only proceed with random disaster checks if enabled
		tickCounter++;
		if (tickCounter < ConfigManager.getDisasterIntervalTicks()) {
			return;
		}
		tickCounter = 0;
		
		// Attempt to trigger a random disaster
		triggerRandomDisaster(server);
	}
	
	/**
	 * Removes any disasters that have passed their expiration time.
	 * Uses an iterator approach to efficiently remove entries while iterating.
	 */
	private static void checkExpiredDisasters() {
	    long currentTime = System.currentTimeMillis();
	    Iterator<Object2LongMap.Entry<String>> iterator = disasterExpirationTimes.object2LongEntrySet().iterator();
	    
	    while (iterator.hasNext()) {
	        Object2LongMap.Entry<String> entry = iterator.next();
	        if (currentTime > entry.getLongValue()) {
	            String key = entry.getKey();
	            activeDisasters.remove(key);
	            iterator.remove(); // Safe removal during iteration
	        }
	    }
	}
	
	/**
	 * Selects a random island and disaster type, then triggers the disaster.
	 * 
	 * @param server The Minecraft server instance
	 */
	private static void triggerRandomDisaster(MinecraftServer server) {
	    // Get all islands and select one randomly
		List<Island> islands = new ArrayList<>(DataManager.getIslands().values());
		if (islands.isEmpty()) return;
		
		// Use ThreadLocalRandom for better performance
		ThreadLocalRandom random = ThreadLocalRandom.current();
		Island island = islands.get(random.nextInt(islands.size()));

		// Skip islands without zones
		if (island.getZone() == null) return;
		
		// Determine possible disasters for the island type
		DisasterType[] possibleDisasters = ISLAND_DISASTER_TYPES.getOrDefault(island.getType(), new DisasterType[0]);
		if (possibleDisasters.length == 0) return;

		// Select a random disaster from the possibilities
		DisasterType disaster = possibleDisasters[random.nextInt(possibleDisasters.length)];

		// Trigger the selected disaster
		triggerDisaster(server, island.getId(), disaster);
	}
}