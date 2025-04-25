package de.nofelix.stormboundisles.game;

import de.nofelix.stormboundisles.StormboundIslesMod;
import de.nofelix.stormboundisles.data.DataManager;
import de.nofelix.stormboundisles.data.Island;
import de.nofelix.stormboundisles.data.Team;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

import java.util.Random;
import java.util.UUID;

public class GameManager {
    // 1 Woche = 7 Tage = 604800 Sekunden = 12.096.000 Ticks (20 Ticks/Sekunde)
    private static final int BUILD_TICKS = 20 * 60 * 60 * 24 * 7; // 1 Woche
    private static final int PVP_TICKS = 20 * 60 * 60 * 24 * 7;   // 1 Woche
    
    public static GamePhase phase = GamePhase.LOBBY;
    private static int phaseTicks = 0;
    
    // BossBar for phase timer
    private static final String BOSSBAR_ID = "sbi_phase";
    private static ServerBossBar phaseBar;
    // Countdown before game start
    private static boolean isStarting = false;
    private static int countdownTicks = 0;
    private static final int COUNTDOWN_TOTAL_TICKS = 20 * 10; // 10 seconds countdown

    private static final Random random = new Random();

    public static void register() {
        StormboundIslesMod.LOGGER.info("Registering GameManager");
        // Restore bossbar on server start when loading from game_state
        ServerLifecycleEvents.SERVER_STARTED.register(server -> setupBossBar(server));
        ServerTickEvents.END_SERVER_TICK.register(GameManager::onServerTick);
    }

    /** Returns the current phase tick count for persistence */
    public static int getPhaseTicks() {
        return phaseTicks;
    }

    /** Restore phase and tick count without resetting phase behavior */
    public static void setPhaseWithoutReset(GamePhase newPhase, int ticks) {
        phase = newPhase;
        phaseTicks = ticks;
    }

    /**
     * Initiates a countdown before starting the build phase.
     */
    public static void startCountdown(MinecraftServer server) {
        isStarting = true;
        countdownTicks = COUNTDOWN_TOTAL_TICKS;
        setupBossBar(server);
        server.getPlayerManager().broadcast(Text.literal("Game starting in " + (COUNTDOWN_TOTAL_TICKS / 20) + " seconds..."), false);
    }

    public static void startGame(MinecraftServer server) {
        StormboundIslesMod.LOGGER.info("Starting game");
        setPhase(GamePhase.BUILD, server);
        phaseTicks = 0;
        teleportPlayersToIslands(server);
        setAllPlayersGameMode(server, GameMode.SURVIVAL);
        server.getPlayerManager().broadcast(Text.literal("Game started! Build phase begins."), false);
        
        // Initialize scoreboard
        ScoreboardManager.initialize(server);
    }

    public static void stopGame(MinecraftServer server) {
        StormboundIslesMod.LOGGER.info("Stopping game");
        setPhase(GamePhase.ENDED, server);
        setAllPlayersGameMode(server, GameMode.ADVENTURE);
        server.getPlayerManager().broadcast(Text.literal("Game stopped."), false);
        
        // Remove bossbar
        if (phaseBar != null) {
            phaseBar.setVisible(false);
        }
    }

    public static void setPhase(GamePhase newPhase, MinecraftServer server) {
        StormboundIslesMod.LOGGER.info("Changing phase from {} to {}", phase, newPhase);
        phase = newPhase;
        phaseTicks = 0;
        
        // Update bossbar and persist
        setupBossBar(server);
        DataManager.saveGameState();
        
        switch (phase) {
            case LOBBY:
                setPvp(server, false);
                setAllPlayersGameMode(server, GameMode.ADVENTURE);
                break;
            case BUILD:
                setPvp(server, false);
                setAllPlayersGameMode(server, GameMode.SURVIVAL);
                break;
            case PVP:
                setPvp(server, true);
                setAllPlayersGameMode(server, GameMode.SURVIVAL);
                server.getPlayerManager().broadcast(Text.literal("PvP phase started!"), false);
                break;
            case ENDED:
                setPvp(server, false);
                setAllPlayersGameMode(server, GameMode.ADVENTURE);
                break;
        }
    }
    
    public static void setupBossBar(MinecraftServer server) {
        // Bossbar erstellen
        phaseBar = new ServerBossBar(
            Text.literal("§b§lStormbound Isles Phase"), // Titel
            BossBar.Color.BLUE,                         // Farbe
            BossBar.Style.PROGRESS                      // Stil
        );

        phaseBar.setPercent(1.0f);      // Fortschritt auf 100%
        phaseBar.setVisible(true);      // Sichtbarkeit aktivieren
        phaseBar.setDarkenSky(false);   // Himmel nicht abdunkeln
        phaseBar.setThickenFog(false);  // Nebel nicht verdichten
        phaseBar.setDragonMusic(false); // Drachenmusik deaktivieren

        // Alle Spieler zur Bossbar hinzufügen
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            phaseBar.addPlayer(player);
        }
    }
    
    private static Text getBossBarTitle() {
        switch (phase) {
            case LOBBY:
                return Text.literal("Lobby Phase - Waiting to start");
            case BUILD:
                return Text.literal("Build Phase - PvP disabled");
            case PVP:
                return Text.literal("PvP Phase - Battle!");
            case ENDED:
                return Text.literal("Game Ended");
            default:
                return Text.literal("Unknown Phase");
        }
    }
    
    private static BossBar.Color getBossBarColor() {
        switch (phase) {
            case LOBBY:
                return BossBar.Color.WHITE;
            case BUILD:
                return BossBar.Color.GREEN;
            case PVP:
                return BossBar.Color.RED;
            case ENDED:
                return BossBar.Color.PURPLE;
            default:
                return BossBar.Color.WHITE;
        }
    }

    private static void setPvp(MinecraftServer server, boolean enabled) {
        StormboundIslesMod.LOGGER.debug("Setting PvP enabled: {}", enabled);
        server.getOverworld().getGameRules().get(GameRules.DO_IMMEDIATE_RESPAWN).set(true, server);
        server.getOverworld().getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, server);
        server.getOverworld().getGameRules().get(GameRules.DO_MOB_SPAWNING).set(true, server);
        server.getOverworld().getGameRules().get(GameRules.DO_INSOMNIA).set(true, server);
        server.getOverworld().getGameRules().get(GameRules.DO_PATROL_SPAWNING).set(true, server);
        server.getOverworld().getGameRules().get(GameRules.DO_TRADER_SPAWNING).set(true, server);
        server.getOverworld().getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(true, server);
        server.getOverworld().getGameRules().get(GameRules.DO_TILE_DROPS).set(true, server);
        server.getOverworld().getGameRules().get(GameRules.DO_ENTITY_DROPS).set(true, server);
        server.setPvpEnabled(enabled);
    }

    private static void setAllPlayersGameMode(MinecraftServer server, GameMode mode) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.interactionManager.getGameMode() != mode) {
                player.changeGameMode(mode);
            }
        }
    }

    private static BlockPos getRandomSpawnPosition(Island island, ServerWorld world) {
        // Try up to 10 random positions within 10-block radius
        for (int i = 0; i < 10; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double dist = random.nextDouble() * 10;
            int x = island.spawnX + (int) Math.round(Math.cos(angle) * dist);
            int z = island.spawnZ + (int) Math.round(Math.sin(angle) * dist);
            int y = island.spawnY;
            if (isAreaClear(world, x, y, z, 10)) {
                return new BlockPos(x + 1, y, z + 1);
            }
        }
        // Fallback to exact spawn
        return new BlockPos(island.spawnX + 1, island.spawnY, island.spawnZ + 1);
    }

    private static boolean isAreaClear(ServerWorld world, int cx, int cy, int cz, int radius) {
        int r2 = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= r2) {
                    BlockPos ground = new BlockPos(cx + dx, cy - 1, cz + dz);
                    BlockPos pos = new BlockPos(cx + dx, cy, cz + dz);
                    if (!world.getBlockState(ground).isSolidBlock(world, ground) ||
                        !world.getBlockState(pos).isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void teleportPlayersToIslands(MinecraftServer server) {
        StormboundIslesMod.LOGGER.info("Teleporting players to their islands");
        for (Team team : DataManager.teams.values()) {
            if (team.islandId == null) {
                StormboundIslesMod.LOGGER.warn("Team {} has no assigned island, skipping teleport", team.name);
                continue;
            }
            Island island = DataManager.islands.get(team.islandId);
            if (island == null || island.zone == null) {
                StormboundIslesMod.LOGGER.warn("Island {} not found or has no defined zone", team.islandId);
                continue;
            }
            for (UUID uuid : team.members) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
                if (player != null) {
                    // Determine teleport target: use custom spawn if defined, else center of zone
                    BlockPos target;
                    if (island.spawnY >= 0) {
                        target = getRandomSpawnPosition(island, server.getOverworld());
                    } else {
                        StormboundIslesMod.LOGGER.error("Island {} has no defined spawn position, unable to teleport player", island.id);
                        // broadcast message to all players
                        server.getPlayerManager().broadcast(Text.literal("Island " + island.id + " has no defined spawn position, unable to teleport " + player.getName()), false);
                        // skip teleportation for this player
                        continue;
                    }
                    player.teleport(server.getOverworld(),
                        target.getX(), target.getY(), target.getZ(),
                        player.getYaw(), player.getPitch());
                }
            }
        }
    }

    private static void onServerTick(MinecraftServer server) {
        // Handle pre-game countdown
        if (isStarting) {
            countdownTicks--;
            if (countdownTicks % 20 == 0 && phaseBar != null) {
                int seconds = countdownTicks / 20;
                server.getPlayerManager().broadcast(Text.literal("Game starting in " + seconds + " seconds..."), false);
                phaseBar.setName(Text.literal("Starting in " + seconds + "s"));
                phaseBar.setPercent((float) countdownTicks / COUNTDOWN_TOTAL_TICKS);
            }
            if (countdownTicks <= 0) {
                isStarting = false;
                startGame(server);
            }
            return;
        }

        if (phase == GamePhase.BUILD) {
            phaseTicks++;
            // Update bossbar progress
            if (phaseBar != null) {
                float progress = 1.0f - ((float) phaseTicks / BUILD_TICKS);
                phaseBar.setPercent(progress);
                
                // Update title with remaining time every minute
                if (phaseTicks % (20 * 60) == 0) {
                    int remainingMinutes = (BUILD_TICKS - phaseTicks) / (20 * 60);
                    phaseBar.setName(Text.literal("Build Phase - " + formatTime(remainingMinutes)));
                    // Persist regularly
                    DataManager.saveGameState();
                }
            }
            
            if (phaseTicks >= BUILD_TICKS) {
                StormboundIslesMod.LOGGER.info("Build phase timer completed ({} ticks)", BUILD_TICKS);
                setPhase(GamePhase.PVP, server);
            }
        } else if (phase == GamePhase.PVP) {
            phaseTicks++;
            // Update bossbar progress
            if (phaseBar != null) {
                float progress = 1.0f - ((float) phaseTicks / PVP_TICKS);
                phaseBar.setPercent(progress);
                
                // Update title with remaining time every minute
                if (phaseTicks % (20 * 60) == 0) {
                    int remainingMinutes = (PVP_TICKS - phaseTicks) / (20 * 60);
                    phaseBar.setName(Text.literal("PvP Phase - " + formatTime(remainingMinutes)));
                    // Persist regularly
                    DataManager.saveGameState();
                }
            }
            
            if (phaseTicks >= PVP_TICKS) {
                StormboundIslesMod.LOGGER.info("PvP phase timer completed ({} ticks)", PVP_TICKS);
                setPhase(GamePhase.ENDED, server);
                server.getPlayerManager().broadcast(Text.literal("Game ended!"), false);
            }
        }
    }
    
    private static String formatTime(int minutes) {
        if (minutes < 60) {
            return minutes + " min";
        }
        
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        
        if (hours < 24) {
            return hours + "h " + remainingMinutes + "m";
        }
        
        int days = hours / 24;
        int remainingHours = hours % 24;
        
        return days + "d " + remainingHours + "h " + remainingMinutes + "m";
    }
}