package com.magmaguy.elitemobs.utils;

import com.magmaguy.magmacore.util.Logger;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utility class for managing debug messages.
 * Debug messages are only shown when debug mode is enabled for a specific player.
 */
public class DebugMessage {

    // Players who have debug mode enabled
    private static final Set<UUID> debugPlayers = new HashSet<>();

    // Matches Bukkit chat colour codes (§ followed by a hex digit / format letter)
    // and the ampersand colour syntax that magmacore's Logger accepts. We strip these
    // before mirroring chat-only debug output to the server log so the log stays clean.
    private static final Pattern CHAT_COLOR_PATTERN = Pattern.compile("[§&][0-9a-fk-orA-FK-OR]");

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
            Logger.warn(message);
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
            Logger.warn(message);
        }
    }

    /**
     * Sends a debug message directly to a player if they have debug enabled, and
     * mirrors the message (color codes stripped) to the server log.
     * <p>
     * The mirror to console is intentional: the per-hit damage breakdown is too
     * long to read in chat during combat and scrolls past in seconds, but it is
     * exactly the information needed for remote diagnosis (combat path picked,
     * which config key was used, per-mob multipliers). Mirroring to the log
     * means a user can run {@code /em debug}, fight a mob, and upload their
     * server log for review — without that mirror the breakdown lives only in
     * their chat and never reaches the maintainer.
     *
     * @param player  The player to send to
     * @param message The message to send (may contain Bukkit §-color codes)
     */
    public static void send(Player player, String message) {
        if (isDebugEnabled(player)) {
            Logger.sendMessage(player, "&8[Debug] &7" + message);
            Logger.warn("[EM-Debug:" + player.getName() + "] " + stripColors(message));
        }
    }

    /**
     * Strips Bukkit / magmacore chat colour codes from a message so it can be
     * written to the server log without escape noise.
     */
    public static String stripColors(String message) {
        if (message == null) return "";
        return CHAT_COLOR_PATTERN.matcher(message).replaceAll("");
    }

    /**
     * Emits a compact single-line damage summary to the server log for the
     * given player. Intended to be called once per damage event after the
     * verbose chat breakdown so admins can {@code grep "[EM-Damage]"} a log
     * file to scan across many hits at once.
     *
     * @param player The player involved (debug must be enabled for them)
     * @param line   The pre-formatted summary line (no color codes expected)
     */
    public static void damageSummary(Player player, String line) {
        if (isDebugEnabled(player)) {
            Logger.warn("[EM-Damage:" + player.getName() + "] " + line);
        }
    }

    /**
     * Clears all debug players (for shutdown).
     */
    public static void shutdown() {
        debugPlayers.clear();
    }
}
