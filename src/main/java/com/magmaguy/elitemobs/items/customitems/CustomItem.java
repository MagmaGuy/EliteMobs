package com.magmaguy.elitemobs.items.customitems;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfig;
import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import com.magmaguy.elitemobs.items.customenchantments.CustomEnchantmentCache;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomItem {

    private static HashMap<String, ItemStack> staticItems = new HashMap<>();

    /**
     * Returns static items. These items have fixed values based on config settings and are immutable regardless of level.
     *
     * @param fileName The name of the file to look for
     * @return Returns an ItemStack ready to be dropped
     */
    public static ItemStack getStaticItem(String fileName) {
        return staticItems.get(fileName);
    }

    private static HashMap<String, CustomItem> dynamicItem = new HashMap<>();

    /**
     * Returns a dynamic item. These items have a blueprint in the configuration files, whose stats may vary based on
     * mob tier level.
     *
     * @param fileName The name of the file to look for
     * @return Returns a variable item object
     */
    public static CustomItem getDynamicItem(String fileName) {
        return dynamicItem.get(fileName);
    }

    private static HashMap<String, CustomItem> limitedItems = new HashMap<>();

    /**
     * Returns a limited item. These items have a blueprint in the configuration files, and vary according to the item
     * tier based on the configuration settings. However, the values defined in the configuration set the maximum value
     * of the enchantments and potion effects set.
     *
     * @param fileName The name of the file to look for
     * @return Returns a variable item object
     */
    public CustomItem getLimitedItem(String fileName) {
        return limitedItems.get(fileName);
    }

    /**
     * Returns a custom item from the whole pool of the plugin's custom items. This may be of any type, and in the case
     * of static items may not use the itemTier value for anything.
     *
     * @param fileName File name of the custom item as it shows up in the directory
     * @param itemTier Tier of the item to be generated, affects dynamic and limited items
     * @return A final ItemStack already ready to be used
     */
    public static ItemStack getCustomItem(String fileName, int itemTier) {
        if (staticItems.containsKey(fileName))
            return staticItems.get(fileName);
        if (dynamicItem.containsKey(fileName))
            //todo: generate dynamic item
            if (limitedItems.containsKey(fileName))
        //todo: generate limited item
    }

    /**
     * Initializes all config items on startup. Needs to run after the config initialization as it relies on those values.
     */
    public static void intializeCustomItems() {
        for (CustomLootConfigFields configFields : CustomLootConfig.getInitializedCustomLootConfigFieldsList())
            new CustomItem(configFields);
    }

    private CustomLootConfigFields customLootConfigFields;
    private String fileName;
    private boolean isEnabled = true;
    private Material material;
    private String name;
    private List<String> lore = new ArrayList<>();
    private HashMap<Enchantment, Integer> enchantments = new HashMap();
    private HashMap<String, Integer> customEnchantments = new HashMap();
    private List<String> potionEffects = new ArrayList<>();
    private String


    /**
     * Generates a CustomItem object. This holds values for limited and dynamic items until a tier is determined for them.
     *
     * @param customLootConfigFields Config fields upon which the values are based.
     */
    public CustomItem(CustomLootConfigFields customLootConfigFields) {
        this.customLootConfigFields = customLootConfigFields;
        parseFileName();
        if (!parseIsEnabled()) return;
        if (!parseMaterial()) return;
        parseName();
        parseLore();
        parseEnchantments();
        parsePotionEffects();
    }

    private void parseFileName() {
        this.fileName = this.customLootConfigFields.getFileName();
    }

    private boolean parseIsEnabled() {
        return this.isEnabled = this.customLootConfigFields.isEnabled();
    }

    private boolean parseMaterial() {
        this.material = this.customLootConfigFields.getMaterial();
        return material != null;
    }

    private void parseName() {
        this.name = this.customLootConfigFields.getName();
    }

    private void parseLore() {
        this.lore = this.customLootConfigFields.getLore();
    }

    private void parseEnchantments() {
        for (String string : this.customLootConfigFields.getEnchantments())
            try {
                String name = string.split(",")[0];
                int level = 1;
                try {
                    level = Integer.parseInt(string.split(",")[1]);
                } catch (Exception ex) {
                    Bukkit.getLogger().warning("[EliteMobs] Custom Item Entry " + this.fileName + " has an invalid enchantment entry.");
                    Bukkit.getLogger().warning("[EliteMobs} Enchantment " + name + " is missing a level.");
                    Bukkit.getLogger().warning("[EliteMobs] Reminder - The correct format for these is [enchantmentName],[level]");
                    Bukkit.getLogger().warning("[EliteMobs] The name should follow the API names and the level should be above 0.");
                    Bukkit.getLogger().warning("[EliteMobs] Defaulting " + name + " to level 1.");
                }

                if (name.equalsIgnoreCase(CustomEnchantmentCache.hunterEnchantment.getKey()) ||
                        name.equalsIgnoreCase(CustomEnchantmentCache.flamethrowerEnchantment.getKey())) {
                    customEnchantments.put(name, level);
                    continue;
                }

                Enchantment enchantment;

                try {
                    enchantment = Enchantment.getByName(name);
                } catch (Exception ex) {
                    Bukkit.getLogger().warning("[EliteMobs] Custom Item Entry " + this.fileName + " has an invalid enchantment entry.");
                    Bukkit.getLogger().warning("[EliteMobs} Enchantment " + name + " is not a valid enchantment. Check the Spigot API for the correct names!");
                    Bukkit.getLogger().warning("[EliteMobs] The invalid entry will be skipped.");
                    continue;
                }

                enchantments.put(enchantment, level);

            } catch (Exception ex) {
                Bukkit.getLogger().warning("[EliteMobs] Invalid enchantment entry for item " + this.fileName);
                Bukkit.getLogger().warning("[EliteMobs] [" + string + "] is not a valid entry and will be ignored.");
                Bukkit.getLogger().warning("[EliteMobs] Reminder - The correct format for these is [enchantmentName],[level]");
                Bukkit.getLogger().warning("[EliteMobs] The name should follow the API names and the level should be above 0.");
            }
    }

    private void parsePotionEffects() {
        this.potionEffects = this.customLootConfigFields.getPotionEffects();
    }

}
