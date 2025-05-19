package de.nofelix.stormboundisles.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Represents a category of commands that can be registered with the
 * CommandManager.
 */
public interface CommandCategory {
    /**
     * Register all commands in this category to the root command.
     * 
     * @param rootCommand The root command to add these commands to
     */
    void register(LiteralArgumentBuilder<ServerCommandSource> rootCommand);
}