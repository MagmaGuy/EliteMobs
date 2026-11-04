package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    /**
     * Minecraft's own English name for the material ("Steak" for COOKED_BEEF), from the table
     * tools/vanilla-item-names/generate.py writes. A material the table lacks, such as one from a newer Minecraft
     * version, is title-cased from its enum name instead ("BREAD" becomes "Bread").
     */
    static String englishName(Material material) {
        String official = OfficialNames.TABLE.get(material.name());
        if (official != null) return official;
        String[] words = material.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder displayName = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (displayName.length() > 0) displayName.append(' ');
            displayName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return displayName.toString();
    }

    /** The official names by material name, read on first use. */
    private static final class OfficialNames {
        private static final String RESOURCE = "/vanilla_item_names.tsv";
        private static final Map<String, String> TABLE = load();

        private static Map<String, String> load() {
            Map<String, String> names = new HashMap<>();
            try (InputStream input = ItemDisplayNames.class.getResourceAsStream(RESOURCE)) {
                if (input == null) {
                    Logger.warn("The jar has no " + RESOURCE.substring(1) + ", so vanilla items show their material names.");
                    return Map.of();
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    int tab = line.indexOf('\t');
                    if (line.startsWith("#") || tab <= 0) continue;
                    names.put(line.substring(0, tab), line.substring(tab + 1));
                }
            } catch (IOException exception) {
                Logger.warn("Could not read " + RESOURCE.substring(1) + ", so vanilla items show their material names: "
                        + exception.getMessage());
                return Map.of();
            }
            return Map.copyOf(names);
        }
    }
}
