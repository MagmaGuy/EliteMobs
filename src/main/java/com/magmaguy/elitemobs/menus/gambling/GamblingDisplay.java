package com.magmaguy.elitemobs.menus.gambling;

import com.magmaguy.elitemobs.config.GamblingConfig;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.Set;

/**
 * Shared display, sound, and message utilities for all gambling games.
 */
public class GamblingDisplay {

    /**
     * Validates a click event for a gambling menu.
     * Cancels the event and returns the player if valid, null otherwise.
     */
    public static Player validateClick(InventoryClickEvent event, Set<Inventory> menus) {
        if (!menus.contains(event.getInventory())) return null;
        if (event.getClickedInventory() == null || !event.getClickedInventory().getType().equals(InventoryType.CHEST)) {
            event.setCancelled(true);
            return null;
        }
        event.setCancelled(true);
        return event.getWhoClicked() instanceof Player player ? player : null;
    }

    /**
     * Plays the standard win sound.
     */
    public static void playWinSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    /**
     * Plays the big win sound (jackpot, blackjack, edge, max streak).
     */
    public static void playBigWinSound(Player player) {
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    /**
     * Plays the lose sound.
     */
    public static void playLoseSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    }

    /**
     * Sends the configured win message to the player.
     */
    public static void sendWinMessage(Player player, double amount, String gameName) {
        player.sendMessage(
                GamblingConfig.getWinMessage()
                        .replace("%amount%", formatCurrency(amount))
                        .replace("%game%", gameName)
        );
    }

    /**
     * Sends the configured lose message to the player.
     */
    public static void sendLoseMessage(Player player, int betAmount, String gameName) {
        player.sendMessage(
                GamblingConfig.getLoseMessage()
                        .replace("%amount%", String.valueOf(betAmount))
                        .replace("%game%", gameName)
        );
    }

    /**
     * Formats a currency amount to 2 decimal places.
     */
    public static String formatCurrency(double amount) {
        return String.format("%.2f", amount);
    }
}
