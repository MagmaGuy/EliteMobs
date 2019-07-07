package com.magmaguy.elitemobs.items.customitems;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import com.magmaguy.elitemobs.items.ScalableItemConstructor;
import com.magmaguy.elitemobs.items.customenchantments.CustomEnchantmentCache;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CustomItem {

    private static HashMap<String, CustomItem> customItems = new HashMap<>();

    public static HashMap<String, CustomItem> getCustomItems() {
        return customItems;
    }

    public static CustomItem getCustomItem(String fileName) {
        if (!fileName.contains(".yml")) fileName += ".yml";
        if (!customItems.containsKey(fileName))
            return null;
        return customItems.get(fileName);
    }

    private static void addCustomItem(String fileName, CustomItem customItem) {
        customItems.put(fileName, customItem);
    }

    // Used to get loot via commands
    private static ArrayList<ItemStack> customItemStackList = new ArrayList<>();

    /**
     * Returns a full list of custom items using their configuration settings
     *
     * @return
     */
    public static ArrayList<ItemStack> getCustomItemStackList() {
        return customItemStackList;
    }

    // Adds custom items to the list used by the getloot GUI
    private static void addCustomItem(CustomItem customItem) {
        customItemStackList.add(customItem.generateDefaultsItemStack());
    }

    // Used to drop static loot using the weighed chance system
    private static HashMap<ItemStack, Double> weighedFixedItems = new HashMap<>();

    /**
     * Returns a full list of weighed static items to be used in the looting process
     *
     * @return
     */
    public static HashMap<ItemStack, Double> getWeighedFixedItems() {
        return weighedFixedItems;
    }

    // Adds weighed static items
    private static void addWeighedFixedItems(CustomItem customItem) {
        weighedFixedItems.put(customItem.generateDefaultsItemStack(), customItem.getDropWeight());
    }

    private static HashMap<Integer, ArrayList<ItemStack>> tieredLoot = new HashMap<>();

    public static HashMap<Integer, ArrayList<ItemStack>> getTieredLoot() {
        return tieredLoot;
    }

    public static void addTieredLoot(CustomItem customItem) {
        ItemStack itemStack = customItem.generateDefaultsItemStack();
        int itemTier = customItem.getItemTier();

        if (tieredLoot.get(itemTier) == null)
            tieredLoot.put(itemTier, new ArrayList<>(Collections.singletonList(itemStack)));

        else {
            ArrayList<ItemStack> list = tieredLoot.get(itemTier);
            list.add(itemStack);
            tieredLoot.put(itemTier, list);
        }
    }

    private static HashMap<Integer, ArrayList<CustomItem>> fixedItems = new HashMap<>();

    public static HashMap<Integer, ArrayList<CustomItem>> getFixedItems() {
        return fixedItems;
    }

    private static void addFixedItem(CustomItem customItem) {
        if (fixedItems.get(customItem.getItemTier()) == null || fixedItems.get(customItem.getItemTier()).isEmpty())
            fixedItems.put(customItem.getItemTier(), new ArrayList<>(Collections.singletonList(customItem)));
        else
            fixedItems.get(customItem.getItemTier()).add(customItem);
    }

    private static List<CustomItem> scalableItems = new ArrayList<>();

    public static List<CustomItem> getScalableItems() {
        return scalableItems;
    }

    private static HashMap<Integer, ArrayList<CustomItem>> limitedItems = new HashMap<>();

    public static HashMap<Integer, ArrayList<CustomItem>> getLimitedItem() {
        return limitedItems;
    }

    private static void addLimitedItem(CustomItem customItem) {
        if (limitedItems.get(customItem.getItemTier()) == null || limitedItems.get(customItem.getItemTier()).isEmpty())
            limitedItems.put(customItem.getItemTier(), new ArrayList<>(Collections.singletonList(customItem)));
        else
            limitedItems.get(customItem.getItemTier()).add(customItem);
    }

    /**
     * Initializes all config items on startup. Needs to run after the config initialization as it relies on those values.
     */
    public static void initializeCustomItems() {
        for (CustomLootConfigFields configFields : CustomLootConfigFields.getCustomLootConfigFields())
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
    private int itemTier;
    private double dropWeight = 0;
    private Scalability scalability;
    private ItemType itemType;


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
        parseItemType();
        parseItemTier();
        //give getloot menu items to work with
        addCustomItem(getFileName(), this);
        addCustomItem(this);
        addTieredLoot(this);
        if (parseDropWeight()) {
            //item is weighed and fixed
            addFixedItem(this);
            addWeighedFixedItems(this);
            this.scalability = Scalability.FIXED;
            return;
        }
        parseScalability();
    }

    private void parseFileName() {
        this.fileName = this.customLootConfigFields.getFileName();
    }

    private boolean parseIsEnabled() {
        return this.isEnabled = this.customLootConfigFields.isEnabled();
    }

    private boolean parseMaterial() {
        try {
            this.material = Material.valueOf(this.customLootConfigFields.getMaterial());
            return true;
        } catch (Exception ex) {
            Bukkit.getLogger().warning("[EliteMobs] Item " + this.fileName + " has an invalid material name: " + this.customLootConfigFields.getItemType());
            Bukkit.getLogger().warning("[EliteMobs] Check the Spigot API for a list of valid API material names or talk to the dev on discord!");
        }

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

    public enum ItemType {
        CUSTOM,
        UNIQUE
    }

    private void parseItemType() {
        if (this.customLootConfigFields.getItemType() == null) this.itemType = ItemType.CUSTOM;
        switch (this.customLootConfigFields.getItemType()) {
            case "custom":
                this.itemType = ItemType.CUSTOM;
                break;
            case "unique":
                this.itemType = ItemType.UNIQUE;
                break;
            default:
                this.itemType = ItemType.CUSTOM;
                Bukkit.getLogger().warning("[EliteMobs] Item " + fileName + " does not have a valid EliteMobs item type. Defaulting to custom.");
                break;
        }
    }

    private boolean parseDropWeight() {
        if (this.customLootConfigFields.getDropWeight() == null) return false;
        if (this.customLootConfigFields.getDropWeight().equalsIgnoreCase("dynamic")) return false;
        try {
            this.dropWeight = Double.parseDouble(this.customLootConfigFields.getDropWeight());
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().warning("[EliteMobs] Item " + this.fileName + " does not have a valid itemWeight.");
        }
        return false;
    }

    public enum Scalability {
        FIXED,
        LIMITED,
        SCALABLE
    }

    private void parseScalability() {
        if (this.customLootConfigFields.getScalability() == null) this.scalability = Scalability.SCALABLE;
        switch (this.customLootConfigFields.getScalability()) {
            case "fixed":
                this.scalability = Scalability.FIXED;
                if (!itemType.equals(ItemType.UNIQUE))
                    addFixedItem(this);
                break;
            case "limited":
                this.scalability = Scalability.LIMITED;
                if (!itemType.equals(ItemType.UNIQUE))
                    addLimitedItem(this);
                break;
            case "scalable":
                this.scalability = Scalability.SCALABLE;
                if (!itemType.equals(ItemType.UNIQUE))
                    scalableItems.add(this);
                break;
            default:
                this.scalability = Scalability.SCALABLE;
                if (!itemType.equals(ItemType.UNIQUE))
                    scalableItems.add(this);
                Bukkit.getLogger().warning("Item " + this.fileName + " does not have a valid scalability type! Defaulting to scalable.");

        }
    }

    private void parseItemTier() {
        this.itemTier = (int) ItemTierFinder.findBattleTier(generateDefaultsItemStack());
    }

    public ItemStack generateDefaultsItemStack() {
        return ItemConstructor.constructItem(
                getName(),
                getMaterial(),
                getEnchantments(),
                getCustomEnchantments(),
                getPotionEffects(),
                getLore());
    }

    public ItemStack generateItemStack(int itemTier) {
        switch (this.scalability) {
            case FIXED:
                return generateDefaultsItemStack();
            case LIMITED:
                return ScalableItemConstructor.constructLimitedItem(itemTier, this);
            case SCALABLE:
                return ScalableItemConstructor.constructScalableItem(itemTier, this);
        }
        return null;
    }

    public CustomLootConfigFields getCustomLootConfigFields() {
        return customLootConfigFields;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public HashMap<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    public HashMap<String, Integer> getCustomEnchantments() {
        return customEnchantments;
    }

    public List<String> getPotionEffects() {
        return potionEffects;
    }

    public int getItemTier() {
        return itemTier;
    }

    public double getDropWeight() {
        return dropWeight;
    }

    public Scalability getScalability() {
        return scalability;
    }

    public ItemType getItemType() {
        return itemType;
    }
}
