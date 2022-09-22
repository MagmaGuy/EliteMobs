package com.magmaguy.elitemobs.items.customitems;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.LootTables;
import com.magmaguy.elitemobs.items.ScalableItemConstructor;
import com.magmaguy.elitemobs.items.customenchantments.*;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class CustomItem {

    private static final HashMap<String, CustomItem> customItems = new HashMap<>();
    // Used to get loot via commands
    private static final ArrayList<ItemStack> customItemStackList = new ArrayList<>();
    // Used to get loot via custom shop, used for efficiency (avoids the double calc of the lore)
    private static final ArrayList<ItemStack> customItemStackShopList = new ArrayList<>();
    // Used to drop static loot using the weighed chance system
    private static final HashMap<ItemStack, Double> weighedFixedItems = new HashMap<>();
    private static final HashMap<Integer, ArrayList<ItemStack>> tieredLoot = new HashMap<>();
    private static final HashMap<Integer, ArrayList<CustomItem>> fixedItems = new HashMap<>();
    private static final List<CustomItem> scalableItems = new ArrayList<>();
    private static final HashMap<Integer, ArrayList<CustomItem>> limitedItems = new HashMap<>();
    private final CustomItemsConfigFields customItemsConfigFields;
    private final HashMap<Enchantment, Integer> enchantments = new HashMap<>();
    private final HashMap<String, Integer> customEnchantments = new HashMap<>();
    @Getter
    private final String permission;
    private String fileName;
    private boolean isEnabled = true;
    private Material material;
    private String name;
    private List<String> lore = new ArrayList<>();
    private List<String> potionEffects = new ArrayList<>();
    private int itemTier;
    private double dropWeight = 0;
    private Scalability scalability;
    private ItemType itemType;
    private double itemLevel = -1;

    /**
     * Generates a CustomItem object. This holds values for limited and dynamic items until a tier is determined for them.
     *
     * @param customItemsConfigFields Config fields upon which the values are based.
     */
    public CustomItem(CustomItemsConfigFields customItemsConfigFields) {
        this.customItemsConfigFields = customItemsConfigFields;
        this.itemLevel = customItemsConfigFields.getLevel();
        if (itemLevel == 0)
            itemLevel = EliteItemManager.getItemLevel(new ItemStack(customItemsConfigFields.getMaterial()));
        this.permission = customItemsConfigFields.getPermission();
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

    public static ArrayList<ItemStack> getCustomItemStackShopList() {
        return customItemStackShopList;
    }

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
        customItemStackList.add(customItem.generateDefaultsItemStack(null, false, null));
        if (customItem.getItemType().equals(ItemType.UNIQUE)) return;
        customItemStackShopList.add(customItem.generateDefaultsItemStack(null, true, null));
    }

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
        ItemStack itemStack = customItem.generateDefaultsItemStack(null, false, null);
        weighedFixedItems.put(itemStack, customItem.getDropWeight());
    }

    public static HashMap<Integer, ArrayList<ItemStack>> getTieredLoot() {
        return tieredLoot;
    }

    public static void addTieredLoot(CustomItem customItem) {
        ItemStack itemStack = customItem.generateDefaultsItemStack(null, false, null);
        int itemTier = customItem.getItemTier();

        if (tieredLoot.get(itemTier) == null)
            tieredLoot.put(itemTier, new ArrayList<>(Collections.singletonList(itemStack)));

        else {
            ArrayList<ItemStack> list = tieredLoot.get(itemTier);
            list.add(itemStack);
            tieredLoot.put(itemTier, list);
        }
    }

    public static HashMap<Integer, ArrayList<CustomItem>> getFixedItems() {
        return fixedItems;
    }

    private static void addFixedItem(CustomItem customItem) {
        if (fixedItems.get(customItem.getItemTier()) == null || fixedItems.get(customItem.getItemTier()).isEmpty())
            fixedItems.put(customItem.getItemTier(), new ArrayList<>(Collections.singletonList(customItem)));
        else
            fixedItems.get(customItem.getItemTier()).add(customItem);
    }

    public static List<CustomItem> getScalableItems() {
        return scalableItems;
    }

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
        for (CustomItemsConfigFields configFields : CustomItemsConfig.getCustomItems().values())
            try {
                new CustomItem(configFields);
            } catch (Exception ex) {
                new WarningMessage("Failed to generate custom item in file " + configFields.getFilename() + " !");
                ex.printStackTrace();
            }
    }

    public Item dropPlayerLoot(Player player, int tier, Location location, EliteEntity eliteEntity) {
        if (!permission.isEmpty() && !player.hasPermission(permission)) return null;
        Item loot = null;
        int itemTier = 0;

        if (AdventurersGuildConfig.isGuildLootLimiter()) {
            itemTier = (int) LootTables.setItemTier(tier);
            if (itemTier > GuildRank.getActiveGuildRank(player) * 10)
                itemTier = GuildRank.getActiveGuildRank(player) * 10;
        } else
            itemTier = tier + 1;

        switch (getScalability()) {
            case LIMITED:
                loot = location.getWorld().dropItem(location,
                        ScalableItemConstructor.constructLimitedItem(itemTier, this, player, eliteEntity));
                break;
            case SCALABLE:
                loot = location.getWorld().dropItem(location,
                        ScalableItemConstructor.constructScalableItem(itemTier + 1, this, player, eliteEntity));
                break;
            case FIXED:
                loot = location.getWorld().dropItem(location, generateItemStack((int) itemLevel + 1, player, eliteEntity));
            default:
        }

        SoulbindEnchantment.addPhysicalDisplay(loot, player);
        loot.setCustomName(loot.getItemStack().getItemMeta().getDisplayName());
        loot.setCustomNameVisible(true);

        return loot;
    }

    private void parseFileName() {
        this.fileName = this.customItemsConfigFields.getFilename();
    }

    private boolean parseIsEnabled() {
        return this.isEnabled = this.customItemsConfigFields.isEnabled();
    }

    private boolean parseMaterial() {
        this.material = this.customItemsConfigFields.getMaterial();
        return material != null;
    }

    private void parseName() {
        this.name = this.customItemsConfigFields.getName();
    }

    private void parseLore() {
        this.lore = this.customItemsConfigFields.getLore();
    }

    private void parseEnchantments() {
        for (String string : this.customItemsConfigFields.getEnchantments())
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
/*
                for (CustomEnchantment customEnchantment : CustomEnchantment.getCustomEnchantments())
                    if (customEnchantment.key.equalsIgnoreCase(name)){
                        customEnchantments.put(name.toLowerCase(), level);
                        break;
                    }
*/

                if (name.equalsIgnoreCase(HunterEnchantment.key) ||
                        name.equalsIgnoreCase(FlamethrowerEnchantment.key) ||
                        name.equalsIgnoreCase(CriticalStrikesEnchantment.key) ||
                        name.equalsIgnoreCase(MeteorShowerEnchantment.key) ||
                        name.equalsIgnoreCase(SoulbindEnchantment.key) ||
                        name.equalsIgnoreCase(DrillingEnchantment.key) ||
                        name.equalsIgnoreCase(IceBreakerEnchantment.key) ||
                        name.equalsIgnoreCase(SummonMerchantEnchantment.key) ||
                        name.equalsIgnoreCase(SummonWolfEnchantment.key) ||
                        name.equalsIgnoreCase(UnbindEnchantment.key) ||
                        name.equalsIgnoreCase(LightningEnchantment.key) ||
                        name.equalsIgnoreCase(PlasmaBootsEnchantment.key) ||
                        name.equalsIgnoreCase(GrapplingHookEnchantment.key) ||
                        name.equalsIgnoreCase(EarthquakeEnchantment.key) ||
                        name.equalsIgnoreCase(LoudStrikesEnchantment.key)) {
                    customEnchantments.put(name.toLowerCase(), level);
                    continue;
                }


                Enchantment enchantment;

                try {
                    enchantment = Enchantment.getByName(name);
                    if (enchantment == null)
                        throw new Exception("Null enchantment");
                } catch (Exception ex) {
                    Bukkit.getLogger().warning("[EliteMobs] Custom Item Entry " + this.fileName + " has an invalid enchantment entry.");
                    Bukkit.getLogger().warning("[EliteMobs] Enchantment " + name + " is not a valid enchantment. Check the Spigot API for the correct names!");
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
        this.potionEffects = this.customItemsConfigFields.getPotionEffects();
    }

    private void parseItemType() {
        if (this.customItemsConfigFields.getItemType() == null) {
            this.itemType = ItemType.CUSTOM;
            return;
        }
        this.itemType = customItemsConfigFields.getItemType();
    }

    private boolean parseDropWeight() {
        if (this.customItemsConfigFields.getDropWeight() == null) return false;
        if (this.customItemsConfigFields.getDropWeight().equalsIgnoreCase("dynamic")) return false;
        try {
            this.dropWeight = Double.parseDouble(this.customItemsConfigFields.getDropWeight());
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().warning("[EliteMobs] Item " + this.fileName + " does not have a valid itemWeight.");
        }
        return false;
    }

    private void parseScalability() {
        if (this.customItemsConfigFields.getScalability() == null) {
            this.scalability = Scalability.SCALABLE;
            return;
        }
        this.scalability = customItemsConfigFields.getScalability();
        switch (this.customItemsConfigFields.getScalability()) {
            case FIXED:
                if (!itemType.equals(ItemType.UNIQUE))
                    addFixedItem(this);
                break;
            case LIMITED:
                if (!itemType.equals(ItemType.UNIQUE))
                    addLimitedItem(this);
                break;
            case SCALABLE:
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
        this.itemTier = (int) Math.round(EliteItemManager.getItemLevel(generateDefaultsItemStack(null, false, null)));
    }

    public ItemStack generateDefaultsItemStack(Player player, boolean showItemWorth, EliteEntity eliteEntity) {
        if (player != null && !permission.isEmpty() && !player.hasPermission(permission)) return null;
        ItemStack itemStack =
                ItemConstructor.constructItem(
                        (int) itemLevel,
                        getName(),
                        getMaterial(),
                        getEnchantments(),
                        getCustomEnchantments(),
                        getPotionEffects(),
                        getLore(),
                        eliteEntity,
                        player,
                        showItemWorth,
                        customItemsConfigFields.getCustomModelID());
        ItemMeta itemMeta = itemStack.getItemMeta();
        //Adds the filename to the persistent data container, useful for several things but mostly used for tracking quest keys
        Objects.requireNonNull(itemMeta).getPersistentDataContainer().set(new NamespacedKey(MetadataHandler.PLUGIN, fileName), PersistentDataType.STRING, fileName);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public ItemStack generateItemStack(int itemTier, Player player, EliteEntity eliteEntity) {
        ItemStack itemStack = null;
        switch (this.scalability) {
            case FIXED:
                itemStack = generateDefaultsItemStack(player, false, eliteEntity);
                break;
            case LIMITED:
                itemStack = ScalableItemConstructor.constructLimitedItem(itemTier, this, player, eliteEntity);
                break;
            case SCALABLE:
                itemStack = ScalableItemConstructor.constructScalableItem(itemTier, this, player, eliteEntity);
        }
        return itemStack;
    }

    public CustomItemsConfigFields getCustomLootConfigFields() {
        return customItemsConfigFields;
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

    public enum ItemType {
        CUSTOM,
        UNIQUE
    }

    public enum Scalability {
        FIXED,
        LIMITED,
        SCALABLE
    }

}
