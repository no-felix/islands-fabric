package de.nofelix.stormboundisles.command.util;

import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Predicate;

/**
 * Utility class for handling command permissions in the Stormbound Isles mod.
 * <p>
 * This class defines standardized permission levels used throughout the command
 * system
 * and provides utility methods for creating permission predicates that can be
 * used with
 * Brigadier's {@code requires()} method.
 */
public class CommandPermissions {
    /** Administrator permission level - highest privileges (ops level 3+) */
    public static final int ADMIN_PERMISSION_LEVEL = 3;

    /** Moderator permission level - elevated privileges (ops level 2+) */
    public static final int MODERATOR_PERMISSION_LEVEL = 2;

    /** Regular player permission level - basic access (no op required) */
    public static final int PLAYER_PERMISSION_LEVEL = 0;

    /**
     * Creates a permission predicate for command requirements.
     * <p>
     * This method returns a predicate that can be used with Brigadier's
     * {@code requires()}
     * method to restrict command access based on permission levels.
     * 
     * @param level The permission level required to execute the command
     * @return A predicate that checks if the command source has the specified
     *         permission level
     */
    public static Predicate<ServerCommandSource> requiresPermissionLevel(int level) {
        return src -> src.hasPermissionLevel(level);
    }
}