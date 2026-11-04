package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;

/**
 * The name menus, lore and messages print for an item. Those are plain strings on the Spigot API, so a vanilla item
 * can't be shown in each player's client language. Its English name goes through the language file instead, like the
 * text around it.
 */
public final class ItemDisplayNames {

    private ItemDisplayNames() {
    }

    /** The item's display name, or its material's name when it has none. */
    public static String of(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null && itemMeta.hasDisplayName() && !itemMeta.getDisplayName().isEmpty())
            return itemMeta.getDisplayName();
        return of(itemStack.getType());
    }

    /** The material's English name ("BREAD" becomes "Bread"), or its translation from the language file. */
    public static String of(Material material) {
        return TranslationsConfig.translateText(englishName(material));
    }

    /**
     * Adds the material's name to the language file while content loads, so {@code /em language custom} lists it
     * before anyone sees the item.
     */
    public static void register(Material material) {
        of(material);
    }

    static String englishName(Material material) {
        String[] words = material.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder displayName = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (displayName.length() > 0) displayName.append(' ');
            displayName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return displayName.toString();
    }
}
