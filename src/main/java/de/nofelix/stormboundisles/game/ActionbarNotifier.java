package de.nofelix.stormboundisles.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Utility class for sending messages to a player's action bar.
 */
public class ActionbarNotifier {

    /**
     * Sends a message to be displayed on the specified player's action bar.
     *
     * @param player  The player to send the message to.
     * @param message The text message to display.
     */
    public static void send(ServerPlayerEntity player, String message) {
        player.sendMessage(Text.literal(message), true);
    }
}