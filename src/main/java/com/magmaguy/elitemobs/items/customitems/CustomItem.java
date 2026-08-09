package com.magmaguy.elitemobs.items.customitems;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.ScalableItemConstructor;
import com.magmaguy.elitemobs.items.customenchantments.CustomEnchantment;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class CustomItem {

    @Getter
    private static final HashMap<String, CustomItem> customItems = new HashMap<>();
    // Used to get loot via commands
    @Getter
    private static final ArrayList<ItemStack> customItemStackList = new ArrayList<>();
    // Used to get loot via custom shop, used for efficiency (avoids the double calc of the lore)
    @Getter
    private static final ArrayList<ItemStack> customItemStackShopList = new ArrayList<>();
    @Getter
    // Used to drop static loot using the weighed chance system
    private static final HashMap<ItemStack, Double> weighedFixedItems = new HashMap<>();
    @Getter
    private static final HashMap<Integer, ArrayList<ItemStack>> tieredLoot = new HashMap<>();
    @Getter
    private static final HashMap<Integer, ArrayList<CustomItem>> fixedItems = new HashMap<>();
    @Getter
    private static final List<CustomItem> scalableItems = new ArrayList<>();
    @Getter
    private static final HashMap<Integer, ArrayList<CustomItem>> limitedItems = new HashMap<>();
    private static final CacheRegenerationLifecycle cacheRegenerationLifecycle = new CacheRegenerationLifecycle();
    private static final Object cacheTaskLock = new Object();
    private static TrackedCacheTask cacheRegenerationTask;
    private static TrackedCacheTask cacheSwapTask;
    @Getter
    private final CustomItemsConfigFields customItemsConfigFields;
    @Getter
    private final HashMap<Enchantment, Integer> enchantments = new HashMap<>();
    @Getter
    private final HashMap<String, Integer> customEnchantments = new HashMap<>();
    @Getter
    private final String permission;
    @Getter
    private List<String> potionEffects = new ArrayList<>();
    @Getter
    private double dropWeight = 0;
    @Getter
    private Scalability scalability;
    @Getter
    private ItemType itemType;
    @Getter
    private int itemLevel = -1;

    /**
     * Generates a CustomItem object. This holds values for limited and dynamic items until a tier is determined for them.
     *
     * @param customItemsConfigFields Config fields upon which the values are based.
     */
    public CustomItem(CustomItemsConfigFields customItemsConfigFields) {
        this.customItemsConfigFields = customItemsConfigFields;
        this.itemLevel = customItemsConfigFields.getLevel();
        if (itemLevel == 0)
            itemLevel = (int) EliteItemManager.getItemLevel(new ItemStack(customItemsConfigFields.getMaterial()));
        this.permission = customItemsConfigFields.getPermission();
        if (!customItemsConfigFields.isEnabled()) return;
        if (customItemsConfigFields.getMaterial() == null) return;
        parseEnchantments();
        parsePotionEffects();
        parseItemType();
        parseItemLevel();
        //give getloot menu items to work with
        addCustomItem(customItemsConfigFields.getFilename(), this);
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

    public static CustomItem getCustomItem(String fileName) {
        if (!fileName.contains(".yml")) fileName += ".yml";
        if (!customItems.containsKey(fileName))
            return null;
        return customItems.get(fileName);
    }

    private static void addCustomItem(String fileName, CustomItem customItem) {
        customItems.put(fileName, customItem);
    }

    // Adds custom items to the list used by the getloot GUI
    private static void addCustomItem(CustomItem customItem) {
        customItemStackList.add(customItem.generateDefaultsItemStack(null, false, null));
        if (isShopExcluded(customItem.getItemType())) return;
        customItemStackShopList.add(customItem.generateDefaultsItemStack(null, true, null));
    }

    // Adds weighed static items
    private static void addWeighedFixedItems(CustomItem customItem) {
        ItemStack itemStack = customItem.generateDefaultsItemStack(null, false, null);
        weighedFixedItems.put(itemStack, customItem.getDropWeight());
    }

    public static void addTieredLoot(CustomItem customItem) {
        ItemStack itemStack = customItem.generateDefaultsItemStack(null, false, null);
        int itemTier = customItem.getItemLevel();

        if (tieredLoot.get(itemTier) == null)
            tieredLoot.put(itemTier, new ArrayList<>(Collections.singletonList(itemStack)));

        else {
            ArrayList<ItemStack> list = tieredLoot.get(itemTier);
            list.add(itemStack);
            tieredLoot.put(itemTier, list);
        }
    }

    private static void addFixedItem(CustomItem customItem) {
        if (fixedItems.get(customItem.getItemLevel()) == null || fixedItems.get(customItem.getItemLevel()).isEmpty())
            fixedItems.put(customItem.getItemLevel(), new ArrayList<>(Collections.singletonList(customItem)));
        else
            fixedItems.get(customItem.getItemLevel()).add(customItem);
    }

    private static void addLimitedItem(CustomItem customItem) {
        if (limitedItems.get(customItem.getItemLevel()) == null || limitedItems.get(customItem.getItemLevel()).isEmpty())
            limitedItems.put(customItem.getItemLevel(), new ArrayList<>(Collections.singletonList(customItem)));
        else
            limitedItems.get(customItem.getItemLevel()).add(customItem);
    }

    /**
     * Initializes all config items on startup. Needs to run after the config initialization as it relies on those values.
     */
    public static void initializeCustomItems() {
        for (CustomItemsConfigFields configFields : CustomItemsConfig.getCustomItems().values())
            try {
                new CustomItem(configFields);
            } catch (Exception ex) {
                Logger.warn("Failed to generate custom item in file " + configFields.getFilename() + " !");
                ex.printStackTrace();
            }
    }

    /**
     * Regenerates all cached ItemStacks used for menus and displays.
     * This is needed because on first boot, resource pack models may not be available yet
     * when items are initially generated (due to plugin load order).
     * Call this after all plugins have finished loading to ensure custom skins are applied.
     */
    public static void regenerateCachedItemStacks() {
        //Rebuilt off the main thread, then swapped in on it.
        //
        //Every item here is constructed from scratch, and constructing one rewrites its whole lore,
        //which recalculates DPS, attack speed and defence. On a server with a few hundred custom
        //items that added up to enough main-thread time for Paper's watchdog to start dumping
        //threads mid-startup. The same construction already runs off the main thread when items are
        //first loaded, so doing it here too is not new ground.
        //
        //The caches are populated by that earlier load, so they stay usable throughout: this only
        //refreshes them with resource-pack models that were not available yet at that point. Worst
        //case, an item shows its pre-pack appearance for a moment longer.
        CacheRegenerationLifecycle.Attempt<CustomItem> attempt = cacheRegenerationLifecycle.beginIf(
                () -> !com.magmaguy.elitemobs.MetadataHandler.shutdownRequested
                        && com.magmaguy.elitemobs.MetadataHandler.PLUGIN != null
                        && com.magmaguy.elitemobs.MetadataHandler.PLUGIN.isEnabled(),
                () -> customItems.values());
        if (attempt == null) return;

        //Starting a newer rebuild makes any older worker/swap stale.
        cancelTrackedCacheTasks();

        BukkitTask scheduledTask;
        try {
            scheduledTask = org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(
                    com.magmaguy.elitemobs.MetadataHandler.PLUGIN,
                    () -> rebuildCachedItemStacks(attempt));
        } catch (RuntimeException exception) {
            if (cacheRegenerationLifecycle.isCurrent(attempt.generation())
                    && !com.magmaguy.elitemobs.MetadataHandler.shutdownRequested) {
                Logger.warn("Failed to schedule the cached item stack refresh.");
                exception.printStackTrace();
            }
            return;
        }

        if (!trackCacheTask(attempt.generation(), scheduledTask, true))
            scheduledTask.cancel();
    }

    private static void rebuildCachedItemStacks(CacheRegenerationLifecycle.Attempt<CustomItem> attempt) {
        try {
            ArrayList<ItemStack> rebuiltItemStackList = new ArrayList<>();
            ArrayList<ItemStack> rebuiltItemStackShopList = new ArrayList<>();
            HashMap<Integer, ArrayList<ItemStack>> rebuiltTieredLoot = new HashMap<>();
            HashMap<ItemStack, Double> rebuiltWeighedFixedItems = new HashMap<>();

            try {
                if (!buildCachedItemStacks(
                        attempt,
                        rebuiltItemStackList,
                        rebuiltItemStackShopList,
                        rebuiltTieredLoot,
                        rebuiltWeighedFixedItems))
                    return;
            } catch (Exception exception) {
                //Existing caches are left alone, so the server keeps the items it already had.
                if (cacheRegenerationLifecycle.isCurrent(attempt.generation())
                        && !com.magmaguy.elitemobs.MetadataHandler.shutdownRequested) {
                    Logger.warn("Failed to refresh the cached item stacks; items will keep the appearance they were first built with.");
                    exception.printStackTrace();
                }
                return;
            }

            if (!cacheRegenerationLifecycle.isCurrent(attempt.generation())) return;

            BukkitTask scheduledSwap;
            try {
                scheduledSwap = org.bukkit.Bukkit.getScheduler().runTask(
                        com.magmaguy.elitemobs.MetadataHandler.PLUGIN,
                        () -> applyRebuiltItemStacks(
                                attempt.generation(),
                                rebuiltItemStackList,
                                rebuiltItemStackShopList,
                                rebuiltTieredLoot,
                                rebuiltWeighedFixedItems));
            } catch (RuntimeException exception) {
                if (cacheRegenerationLifecycle.isCurrent(attempt.generation())
                        && !com.magmaguy.elitemobs.MetadataHandler.shutdownRequested) {
                    Logger.warn("Failed to apply the refreshed cached item stacks.");
                    exception.printStackTrace();
                }
                return;
            }

            if (!trackCacheTask(attempt.generation(), scheduledSwap, false))
                scheduledSwap.cancel();
        } finally {
            clearTrackedCacheTask(attempt.generation(), true);
        }
    }

    private static boolean buildCachedItemStacks(CacheRegenerationLifecycle.Attempt<CustomItem> attempt,
                                                 ArrayList<ItemStack> itemStackList,
                                                 ArrayList<ItemStack> itemStackShopList,
                                                 HashMap<Integer, ArrayList<ItemStack>> tieredLootTarget,
                                                 HashMap<ItemStack, Double> weighedFixedItemsTarget) {
        // Regenerate all cached ItemStacks with proper skins
        for (CustomItem customItem : attempt.snapshot())
            if (!cacheRegenerationLifecycle.runIfCurrent(
                    attempt.generation(),
                    () -> appendCachedItemStacks(
                            customItem,
                            itemStackList,
                            itemStackShopList,
                            tieredLootTarget,
                            weighedFixedItemsTarget)))
                return false;

        return cacheRegenerationLifecycle.isCurrent(attempt.generation());
    }

    private static void appendCachedItemStacks(CustomItem customItem,
                                               ArrayList<ItemStack> itemStackList,
                                               ArrayList<ItemStack> itemStackShopList,
                                               HashMap<Integer, ArrayList<ItemStack>> tieredLootTarget,
                                               HashMap<ItemStack, Double> weighedFixedItemsTarget) {
        if (customItem.getCustomItemsConfigFields() == null || !customItem.getCustomItemsConfigFields().isEnabled())
            return;
        if (customItem.getCustomItemsConfigFields().getMaterial() == null) return;

        //Built once and copied, rather than built three times. This is the same call with the
        //same arguments each time and the result is deterministic, so the extra two builds were
        //producing identical stacks at full price — and that price is high, since constructing
        //an item rewrites its whole lore, which recalculates DPS, attack speed and defence.
        //Copies rather than one shared instance, so each list still owns a separate stack the
        //way it did before.
        ItemStack defaultsItemStack = customItem.generateDefaultsItemStack(null, false, null);

        // Regenerate loot menu items
        itemStackList.add(defaultsItemStack);
        if (!isShopExcluded(customItem.getItemType()))
            itemStackShopList.add(customItem.generateDefaultsItemStack(null, true, null));

        // Regenerate tiered loot
        ItemStack itemStack = defaultsItemStack.clone();
        int itemTier = customItem.getItemLevel();
        if (tieredLootTarget.get(itemTier) == null)
            tieredLootTarget.put(itemTier, new ArrayList<>(Collections.singletonList(itemStack)));
        else
            tieredLootTarget.get(itemTier).add(itemStack);

        // Regenerate weighed fixed items
        if (customItem.getScalability() == Scalability.FIXED && customItem.getDropWeight() > 0) {
            ItemStack weighedStack = defaultsItemStack.clone();
            weighedFixedItemsTarget.put(weighedStack, customItem.getDropWeight());
        }
    }

    private static void applyRebuiltItemStacks(long generation,
                                               ArrayList<ItemStack> rebuiltItemStackList,
                                               ArrayList<ItemStack> rebuiltItemStackShopList,
                                               HashMap<Integer, ArrayList<ItemStack>> rebuiltTieredLoot,
                                               HashMap<ItemStack, Double> rebuiltWeighedFixedItems) {
        try {
            cacheRegenerationLifecycle.runIfCurrent(generation, () -> {
                //Refilled in place inside one main-thread task, so no main-thread reader ever observes a partially built cache. The collections are static final and aliased via their getters, so the references themselves must not be swapped.
                customItemStackList.clear();
                customItemStackList.addAll(rebuiltItemStackList);
                customItemStackShopList.clear();
                customItemStackShopList.addAll(rebuiltItemStackShopList);
                tieredLoot.clear();
                tieredLoot.putAll(rebuiltTieredLoot);
                weighedFixedItems.clear();
                weighedFixedItems.putAll(rebuiltWeighedFixedItems);
            });
        } finally {
            clearTrackedCacheTask(generation, false);
        }
    }

    /**
     * Invalidates the active rebuild, waits for any currently constructed item to finish,
     * and cancels both the worker and a queued main-thread cache swap.
     */
    public static void shutdownCacheRegeneration() {
        cacheRegenerationLifecycle.cancel();
        cancelTrackedCacheTasks();
    }

    private static boolean trackCacheTask(long generation, BukkitTask task, boolean regenerationTask) {
        return cacheRegenerationLifecycle.runIfCurrent(generation, () -> {
            synchronized (cacheTaskLock) {
                TrackedCacheTask trackedCacheTask = new TrackedCacheTask(generation, task);
                if (regenerationTask)
                    cacheRegenerationTask = trackedCacheTask;
                else
                    cacheSwapTask = trackedCacheTask;
            }
        });
    }

    private static void clearTrackedCacheTask(long generation, boolean regenerationTask) {
        synchronized (cacheTaskLock) {
            TrackedCacheTask trackedCacheTask = regenerationTask ? cacheRegenerationTask : cacheSwapTask;
            if (trackedCacheTask == null || trackedCacheTask.generation() != generation) return;
            if (regenerationTask)
                cacheRegenerationTask = null;
            else
                cacheSwapTask = null;
        }
    }

    private static void cancelTrackedCacheTasks() {
        TrackedCacheTask regenerationTask;
        TrackedCacheTask swapTask;
        synchronized (cacheTaskLock) {
            regenerationTask = cacheRegenerationTask;
            swapTask = cacheSwapTask;
            cacheRegenerationTask = null;
            cacheSwapTask = null;
        }
        if (regenerationTask != null) regenerationTask.task().cancel();
        if (swapTask != null) swapTask.task().cancel();
    }

    private record TrackedCacheTask(long generation, BukkitTask task) {
    }

    public static int limitItemLevel(Player player, int originalLevel) {
        // Skill-based gear restriction now handles equipping, not drops
        // Items drop at their original level
        return originalLevel;
    }

    public Item dropPlayerLoot(Player player, int tier, Location location, EliteEntity eliteEntity) {
        if (!permission.isEmpty() && !player.hasPermission(permission)) return null;
        Item loot = null;
        int itemTier = limitItemLevel(player, tier);

        switch (getScalability()) {
            case LIMITED:
                loot = location.getWorld().dropItem(location,
                        ScalableItemConstructor.constructLimitedItem(itemTier, this, player, eliteEntity));
                break;
            case SCALABLE:
                loot = location.getWorld().dropItem(location,
                        ScalableItemConstructor.constructScalableItem(itemTier, this, player, eliteEntity));
                break;
            case FIXED:
                loot = location.getWorld().dropItem(location, generateItemStack(itemLevel, player, eliteEntity));
            default:
        }

        SoulbindEnchantment.addPhysicalDisplay(loot, player);
        loot.setCustomName(loot.getItemStack().getItemMeta().getDisplayName());
        loot.setCustomNameVisible(true);

        return loot;
    }

    public Item dropPlayerLootExact(Player player, int level, Location location, EliteEntity eliteEntity) {
        if (!permission.isEmpty() && !player.hasPermission(permission)) return null;
        ItemStack itemStack = generateItemStackExact(level, player, eliteEntity);
        if (itemStack == null) return null;
        Item loot = location.getWorld().dropItem(location, itemStack);
        SoulbindEnchantment.addPhysicalDisplay(loot, player);
        loot.setCustomName(loot.getItemStack().getItemMeta().getDisplayName());
        loot.setCustomNameVisible(true);
        return loot;
    }

    private void parseEnchantments() {
        for (String string : this.customItemsConfigFields.getEnchantments())
            try {
                String name = string.split(",")[0];
                int level = 1;
                try {
                    level = Integer.parseInt(string.split(",")[1]);
                } catch (Exception ex) {
                    Logger.warn("Custom Item Entry " + customItemsConfigFields.getFilename() + " has an invalid enchantment entry.");
                    Logger.warn("Enchantment " + name + " is missing a level.");
                    Logger.warn("Reminder - The correct format for these is [enchantmentName],[level]");
                    Logger.warn("The name should follow the API names and the level should be above 0.");
                    Logger.warn("Defaulting " + name + " to level 1.");
                }

                if (CustomEnchantment.isCustomEnchantment(name)) {
                    customEnchantments.put(name.toLowerCase(Locale.ROOT), level);
                    continue;
                }

                Enchantment enchantment;
                try {
                    enchantment = Enchantment.getByName(name);
                    if (enchantment == null)
                        throw new Exception("Null enchantment");
                } catch (Exception ex) {
                    Logger.warn("Custom Item Entry " + customItemsConfigFields.getFilename() + " has an invalid enchantment entry.");
                    Logger.warn("Enchantment " + name + " is not a valid enchantment. Check the Spigot API for the correct names!");
                    Logger.warn("The invalid entry will be skipped.");
                    continue;
                }

                enchantments.put(enchantment, level);

            } catch (Exception ex) {
                Logger.warn("Invalid enchantment entry for item " + customItemsConfigFields.getFilename());
                Logger.warn("[" + string + "] is not a valid entry and will be ignored.");
                Logger.warn("Reminder - The correct format for these is [enchantmentName],[level]");
                Logger.warn("The name should follow the API names and the level should be above 0.");
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
            Logger.warn("Item " + customItemsConfigFields.getFilename() + " does not have a valid itemWeight.");
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
                Logger.warn("Item " + customItemsConfigFields.getFilename() + " does not have a valid scalability type! Defaulting to scalable.");

        }
    }

    private void parseItemLevel() {
        this.itemLevel = (int) Math.round(EliteItemManager.getItemLevel(generateDefaultsItemStack(null, false, null)));
    }

    public ItemStack generateDefaultsItemStack(Player player, boolean showItemWorth, EliteEntity eliteEntity) {
        return generateDefaultsItemStack(player, showItemWorth, eliteEntity, false);
    }

    public ItemStack generateDefaultsItemStack(Player player, boolean showItemWorth, EliteEntity eliteEntity, boolean bypass) {
        if (!bypass && player != null && !permission.isEmpty() && !player.hasPermission(permission)) return null;
        ItemStack itemStack =
                ItemConstructor.constructItem(
                        itemLevel,
                        customItemsConfigFields.getName(),
                        customItemsConfigFields.getMaterial(),
                        getEnchantments(),
                        getCustomEnchantments(),
                        getPotionEffects(),
                        customItemsConfigFields.getLore(),
                        eliteEntity,
                        player,
                        showItemWorth,
                        customItemsConfigFields.getCustomModelID(),
                        customItemsConfigFields.getEquipmentModelID(),
                        customItemsConfigFields.isSoulbound(),
                        getCustomItemsConfigFields().getFilename(),
                        customItemsConfigFields.getScriptedItem()
                );
        ItemMeta itemMeta = itemStack.getItemMeta();
        //Adds the filename to the persistent data container, useful for several things but mostly used for tracking quest keys
//        Objects.requireNonNull(itemMeta).getPersistentDataContainer().set(new NamespacedKey(MetadataHandler.PLUGIN, customItemsConfigFields.getFilename()), PersistentDataType.STRING, customItemsConfigFields.getFilename());
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public ItemStack generateItemStack(int itemTier, Player player, EliteEntity eliteEntity) {
        ItemStack itemStack = null;
        //This can happen when doing drop tables, the loot is not yet assigned to anyone
        if (player != null)
            itemTier = limitItemLevel(player, itemTier);
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

    public ItemStack generateItemStackExact(int itemTier, Player player, EliteEntity eliteEntity) {
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

    public enum ItemType {
        /**
         * Default. Item enters the random elite drop pool AND is purchasable in
         * shops (CustomShopMenu).
         */
        CUSTOM,
        /**
         * Boss-only / quest-only. Item is excluded from the random elite drop pool
         * AND from shops — it can only be obtained through the explicit drop tables
         * of specific bosses, treasure chests, or quest rewards.
         */
        UNIQUE,
        /**
         * Drops only. Item enters the random elite drop pool just like CUSTOM, but
         * is excluded from shops — players can find it as a normal elite drop but
         * can never simply buy it. Useful for items that should feel earned but
         * don't need to be locked to a specific boss.
         */
        DROPPABLE
    }

    /**
     * Items of these types are kept out of the shop's randomized inventory
     * ({@link #customItemStackShopList}). Drop-pool eligibility is controlled
     * separately in {@link #parseScalability()}.
     */
    private static boolean isShopExcluded(ItemType type) {
        return type == ItemType.UNIQUE || type == ItemType.DROPPABLE;
    }

    public enum Scalability {
        FIXED,
        LIMITED,
        SCALABLE
    }

}
