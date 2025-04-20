package de.felix.stormboundisles.phases;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

/**
 * Handles phase transitions and timers.
 */
public class PhaseManager {
	private static PhaseManager instance;
	private GamePhase phase = GamePhase.WAITING;
	private long phaseEndTimestamp = -1;

	private PhaseManager() {
	}

	public static PhaseManager getInstance() {
		if (instance == null) instance = new PhaseManager();
		return instance;
	}

	public void setPhase(GamePhase newPhase, long durationMillis, MinecraftServer server) {
		this.phase = newPhase;
		this.phaseEndTimestamp = (durationMillis > 0) ? (System.currentTimeMillis() + durationMillis) : -1;
		announcePhase(server);
	}

	public void tick(MinecraftServer server) {
		if (phaseEndTimestamp > 0 && System.currentTimeMillis() >= phaseEndTimestamp) {
			switch (phase) {
				case GRACE -> setPhase(GamePhase.BATTLE, 0, server);
				case BATTLE -> setPhase(GamePhase.END, 0, server);
				default -> {
				}
			}
		}
	}

	public GamePhase getPhase() {
		return phase;
	}

	private void announcePhase(MinecraftServer server) {
		String msg = switch (phase) {
			case WAITING -> "§7Warte auf Start...";
			case GRACE -> "§aGrace-Phase gestartet!";
			case BATTLE -> "§cBattle-Phase gestartet!";
			case END -> "§eSpiel beendet!";
		};
		server.getPlayerManager().broadcast(Text.literal(msg), false);
	}
}