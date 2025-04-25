package de.nofelix.stormboundisles.util;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Contains constant values used throughout the mod.
 */
public class Constants {
	/** Prefix used for mod-related messages. */
	public static final Text PREFIX = Text.literal("[Stormbound Isles] ").formatted(Formatting.BLUE);
	/** Message displayed when a player tries to execute a command without permission. */
	public static final Text NO_PERMISSION = PREFIX.copy().append(Text.literal("You don't have permission to do that!").formatted(Formatting.RED));
	/** Message displayed when a non-player entity tries to execute a player-only command. */
	public static final Text PLAYER_ONLY = PREFIX.copy().append(Text.literal("This command can only be executed by players!").formatted(Formatting.RED));
	/** Message displayed when a command sender provides invalid arguments. */
	public static final Text INVALID_ARGUMENTS = PREFIX.copy().append(Text.literal("Invalid arguments!").formatted(Formatting.RED));
	/** Message displayed when a specified player cannot be found. */
	public static final Text PLAYER_NOT_FOUND = PREFIX.copy().append(Text.literal("Player not found!").formatted(Formatting.RED));
	/** Message displayed when a specified team cannot be found. */
	public static final Text TEAM_NOT_FOUND = PREFIX.copy().append(Text.literal("Team not found!").formatted(Formatting.RED));
}
