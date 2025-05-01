package de.nofelix.stormboundisles.command.util;

import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Predicate;

/**
 * Utility class for handling command permissions.
 */
public class CommandPermissions {
    // Permission levels
    public static final int ADMIN_PERMISSION_LEVEL = 3;
    public static final int MODERATOR_PERMISSION_LEVEL = 2;
    public static final int PLAYER_PERMISSION_LEVEL = 0;
    
    /**
     * Creates a permission predicate for command requirements.
     * 
     * @param level The permission level required
     * @return A predicate that checks if the command source has the specified permission level
     */
    public static Predicate<ServerCommandSource> requiresPermissionLevel(int level) {
        return src -> src.hasPermissionLevel(level);
    }
}