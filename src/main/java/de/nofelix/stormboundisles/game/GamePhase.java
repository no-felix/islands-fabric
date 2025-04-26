package de.nofelix.stormboundisles.game;

/**
 * Represents the different phases of the Stormbound Isles game.
 */
public enum GamePhase {
	/** The initial phase before the game starts, players are typically in a lobby area. */
	LOBBY,
	/** The phase where teams build their bases and defenses. PvP is usually disabled. */
	BUILD,
	/** The phase where PvP combat is enabled between teams. */
	PVP,
	/** The phase after the game has concluded. */
	ENDED
}