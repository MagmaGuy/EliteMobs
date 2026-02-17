package com.magmaguy.elitemobs.utils;

import com.magmaguy.magmacore.util.Logger;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Utility class for managing debug messages.
 * Debug messages are only shown when debug mode is enabled for a specific player.
 */
public class DebugMessage {

    // Players who have debug mode enabled
    private static final Set<UUID> debugPlayers = new HashSet<>();

    private DebugMessage() {
        // Utility class
    }

    /**
     * Toggles debug mode for a player.
     *
     * @param player The player to toggle debug for
     * @return true if debug is now enabled, false if disabled
     */
    public static boolean toggleDebug(Player player) {
        if (debugPlayers.contains(player.getUniqueId())) {
            debugPlayers.remove(player.getUniqueId());
            return false;
        }
        debugPlayers.add(player.getUniqueId());
        return true;
    }

    /**
     * Checks if debug mode is enabled for a player.
     *
     * @param player The player to check
     * @return true if debug is enabled
     */
    public static boolean isDebugEnabled(Player player) {
        return player != null && debugPlayers.contains(player.getUniqueId());
    }

    /**
     * Checks if any player has debug mode enabled.
     *
     * @return true if at least one player has debug enabled
     */
    public static boolean isAnyDebugEnabled() {
        return !debugPlayers.isEmpty();
    }

    /**
     * Logs a debug message to console if any player has debug enabled.
     * Use this for global combat debug messages.
     *
     * @param message The message to log
     */
    public static void log(String message) {
        if (isAnyDebugEnabled()) {
            Logger.debug(message);
        }
    }

    /**
     * Logs a debug message to console if the specified player has debug enabled.
     *
     * @param player  The player context
     * @param message The message to log
     */
    public static void log(Player player, String message) {
        if (isDebugEnabled(player)) {
            Logger.debug(message);
        }
    }

    /**
     * Sends a debug message directly to a player if they have debug enabled.
     *
     * @param player  The player to send to
     * @param message The message to send
     */
    public static void send(Player player, String message) {
        if (isDebugEnabled(player)) {
            Logger.sendMessage(player, "&8[Debug] &7" + message);
        }
    }

    /**
     * Clears all debug players (for shutdown).
     */
    public static void shutdown() {
        debugPlayers.clear();
    }
}
