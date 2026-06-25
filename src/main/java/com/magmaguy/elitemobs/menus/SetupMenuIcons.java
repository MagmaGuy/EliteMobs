package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.utils.CustomModelAdder;
import org.bukkit.inventory.ItemStack;

/**
 * Constants and utilities for setup menu icons.
 * Uses item_model (1.21.4+) to display custom icons when the resource pack is loaded.
 * Falls back to colored stained glass panes when resource pack is not loaded.
 */
public class SetupMenuIcons {

    // Item model IDs for setup menu icons (1.21.4+ format)
    // These reference files in assets/elitemobs/items/ui/

    /** Can't download automatically: account not linked */
    public static final String MODEL_LOCKED_UNLINKED = "elitemobs:ui/lockedunlinked";

    /** Can't download automatically: didn't pay for it */
    public static final String MODEL_LOCKED_UNPAID = "elitemobs:ui/lockedunpaid";

    /** Can download automatically */
    public static final String MODEL_UNLOCKED = "elitemobs:ui/unlocked";

    /** Installed & active */
    public static final String MODEL_CHECKMARK = "elitemobs:ui/checkmark";

    /** Installed & deactivated */
    public static final String MODEL_GRAY_X = "elitemobs:ui/gray_x";

    /** Installed, has update, not linked */
    public static final String MODEL_UPDATE_UNLINKED = "elitemobs:ui/updateunlinked";

    /** Installed, has update, no access */
    public static final String MODEL_UPDATE_UNPAID = "elitemobs:ui/updateunpaid";

    /** Installed, has update, can click to update */
    public static final String MODEL_UPDATE = "elitemobs:ui/update";

    /** Red cross: no Nightbreak token linked */
    public static final String MODEL_RED_CROSS = "elitemobs:ui/redcross";

    /** Yellow crown: content available to download, or updates pending */
    public static final String MODEL_CROWN_YELLOW = "elitemobs:ui/yellowcrown";

    /**
     * Applies a custom item model to an ItemStack for setup menu display.
     * Uses the 1.21.4+ item_model system via CustomModelAdder.
     * @param itemStack The ItemStack to modify
     * @param modelId The item model ID (e.g., "elitemobs:ui/checkmark")
     */
    public static void applyItemModel(ItemStack itemStack, String modelId) {
        CustomModelAdder.addCustomModel(itemStack, modelId);
    }
}
