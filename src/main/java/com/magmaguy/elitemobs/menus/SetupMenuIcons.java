package com.magmaguy.elitemobs.menus;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Constants and utilities for setup menu icons.
 * Uses custom model data to display custom icons when the resource pack is loaded.
 * Falls back to colored stained glass panes when resource pack is not loaded.
 */
public class SetupMenuIcons {

    // Custom Model Data values for setup menu icons
    // Range: 31180-31187 (continuing from existing 31173-31175)

    /** Not downloaded, no Nightbreak token - locked padlock with chain */
    public static final int CMD_LOCKED_CHAIN = 31180;

    /** Needs access (didn't pay) - locked padlock with coin */
    public static final int CMD_LOCKED_COIN = 31181;

    /** Not downloaded, has access - unlocked padlock */
    public static final int CMD_UNLOCKED = 31182;

    /** Installed and active - golden key */
    public static final int CMD_KEY_GOLD = 31183;

    /** Downloaded but not installed/active - gray key */
    public static final int CMD_KEY_GRAY = 31184;

    /** Out of date, no token - key with chain */
    public static final int CMD_KEY_CHAIN = 31185;

    /** Out of date, no access - key with coin */
    public static final int CMD_KEY_COIN = 31186;

    /** Out of date, can update - key with star */
    public static final int CMD_KEY_STAR = 31187;

    /**
     * Applies custom model data to an ItemStack for setup menu display.
     * @param itemStack The ItemStack to modify
     * @param customModelData The CMD value to apply
     */
    public static void applyCustomModelData(ItemStack itemStack, int customModelData) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(customModelData);
            itemStack.setItemMeta(meta);
        }
    }
}
