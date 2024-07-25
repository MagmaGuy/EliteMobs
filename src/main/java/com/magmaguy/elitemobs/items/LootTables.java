package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.ProceduralItemGenerationSettingsConfig;
import com.magmaguy.elitemobs.config.SpecialItemSystemsConfig;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.WeightedProbability;
import com.magmaguy.magmacore.util.Logger;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import static com.magmaguy.elitemobs.utils.WeightedProbability.pickWeighedProbability;

/**
 * Created by MagmaGuy on 04/06/2017.
 */
public class LootTables implements Listener {

    private static boolean proceduralItemsOn;
    private static boolean customItemsOn;
    private static boolean weighedItemsExist;
    private static boolean fixedItemsExist;
    private static boolean limitedItemsExist;
    private static boolean scalableItemsExist;

    public static void generatePlayerLoot(EliteEntity eliteEntity) {
        if (eliteEntity.isTriggeredAntiExploit()) return;
        for (Player player : eliteEntity.getDamagers().keySet()) {

            if (player.hasMetadata("NPC") || !PlayerData.isInMemory(player.getUniqueId())) continue;

            if (eliteEntity.getDamagers().get(player) / eliteEntity.getMaxHealth() < 0.1) continue;

            if (!eliteEntity.isEliteLoot()) continue;

            double itemLevel = setItemTier(eliteEntity.getLevel());
            double eliteLevel = eliteEntity.getLevel();
            if (AdventurersGuildConfig.isGuildLootLimiter()) {
                if (itemLevel > GuildRank.getActiveGuildRank(player) * 10) {
                    itemLevel = GuildRank.getActiveGuildRank(player) * 10D;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(AdventurersGuildConfig.getLootLimiterMessage()));
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 20 * 10L);
                }
            }

            if (eliteEntity.getPower("bonus_coins.yml") == null)
                new ItemLootShower(itemLevel, eliteLevel, eliteEntity.getUnsyncedLivingEntity().getLocation(), player);

            if (!(eliteEntity.isRandomLoot())) continue;

            if (AdventurersGuildConfig.isGuildLootLimiter())
                generateLoot((int) Math.floor(itemLevel), eliteEntity, player);
            else
                generateLoot(eliteEntity, player);

            if (SpecialItemSystemsConfig.isDropSpecialLoot()) {
                if (eliteEntity instanceof CustomBossEntity customBossEntity &&
                        customBossEntity.getCustomBossesConfigFields().getHealthMultiplier() > 1.0 &&
                        ThreadLocalRandom.current().nextDouble() < SpecialItemSystemsConfig.getBossChanceToDrop())
                    generateSpecialLoot(player, 0, eliteEntity);
                else if (eliteEntity instanceof CustomBossEntity &&
                        ThreadLocalRandom.current().nextDouble() < SpecialItemSystemsConfig.getNonEliteChanceToDrop())
                    generateSpecialLoot(player, 0, eliteEntity);
            }
        }
    }


    public static void initialize() {
        proceduralItemsOn = ProceduralItemGenerationSettingsConfig.isDoProceduralItemDrops();
        customItemsOn = ItemSettingsConfig.isDoEliteMobsLoot() && !CustomItem.getCustomItemStackList().isEmpty();
        weighedItemsExist = CustomItem.getWeighedFixedItems() != null && !CustomItem.getWeighedFixedItems().isEmpty();
        fixedItemsExist = CustomItem.getFixedItems() != null && !CustomItem.getFixedItems().isEmpty();
        limitedItemsExist = CustomItem.getLimitedItems() != null && !CustomItem.getLimitedItems().isEmpty();
        scalableItemsExist = CustomItem.getScalableItems() != null && !CustomItem.getScalableItems().isEmpty();
    }

    private static ItemStack generateLoot(EliteEntity eliteEntity, Player player) {

        int mobTier = (int) MobTierCalculator.findMobTier(eliteEntity);

        /*
        Add some wiggle room to avoid making obtaining loot too linear
         */
        int itemTier = (int) setItemTier(mobTier);

        return generateLoot(itemTier, eliteEntity, player);

    }

    public static ItemStack generateLoot(int itemTier, EliteEntity eliteEntity, Player player) {

         /*
        Handle the odds of an item dropping
         */
        double baseChance = ItemSettingsConfig.getFlatDropRate();
        if (eliteEntity instanceof RegionalBossEntity)
            baseChance = ItemSettingsConfig.getRegionalBossNonUniqueDropRate();
        double dropChanceBonus = ItemSettingsConfig.getLevelIncreaseDropRate() * itemTier;

        if (ThreadLocalRandom.current().nextDouble() > baseChance + dropChanceBonus) return null;

        HashMap<String, Double> weightedProbability = new HashMap<>();
        if (proceduralItemsOn) weightedProbability.put("procedural", ItemSettingsConfig.getProceduralItemWeight());
        if (customItemsOn) {
            if (weighedItemsExist) weightedProbability.put("weighed", ItemSettingsConfig.getWeighedItemWeight());
            if (fixedItemsExist) if (CustomItem.getFixedItems().containsKey(itemTier))
                weightedProbability.put("fixed", ItemSettingsConfig.getFixedItemWeight());
            if (limitedItemsExist) weightedProbability.put("limited", ItemSettingsConfig.getLimitedItemWeight());
            if (scalableItemsExist) weightedProbability.put("scalable", ItemSettingsConfig.getScalableItemWeight());
        }

        String selectedLootSystem = pickWeighedProbability(weightedProbability);

        if (selectedLootSystem == null) {
            Logger.info("Your EliteMobs loot configuration resulted in no loot getting dropped. This is not a bug. " + "If you want! players to be able to progress at all in the EliteMobs plugin, review your configuration settings.");
            return null;
        }

        switch (selectedLootSystem) {
            case "procedural":
                return dropProcedurallyGeneratedItem(itemTier, eliteEntity, player);
            case "weighed":
                return dropWeighedFixedItem(eliteEntity, player);
            case "fixed":
                return dropFixedItem(eliteEntity, itemTier, player);
            case "limited":
                return dropLimitedItem(eliteEntity, itemTier, player);
            case "scalable":
                return dropScalableItem(eliteEntity, itemTier, player);
        }

        return null;

    }

    /**
     * Used for "fake" drops. Currently this means it's used by the SimLootHandler, reason will default to shop.
     *
     * @param itemLevel
     * @param location
     * @param player
     * @return
     */
    public static ItemStack generateLoot(int itemLevel, Location location, Player player) {

         /*
        Handle the odds of an item dropping
         */
        double baseChance = ItemSettingsConfig.getFlatDropRate();
        double dropChanceBonus = ItemSettingsConfig.getLevelIncreaseDropRate() * itemLevel;

        if (ThreadLocalRandom.current().nextDouble() > baseChance + dropChanceBonus) return null;

        HashMap<String, Double> weightedProbability = new HashMap<>();
        if (proceduralItemsOn) weightedProbability.put("procedural", ItemSettingsConfig.getProceduralItemWeight());
        if (customItemsOn) {
            if (weighedItemsExist) weightedProbability.put("weighed", ItemSettingsConfig.getWeighedItemWeight());
            if (fixedItemsExist) if (CustomItem.getFixedItems().containsKey(itemLevel))
                weightedProbability.put("fixed", ItemSettingsConfig.getFixedItemWeight());
            if (limitedItemsExist) weightedProbability.put("limited", ItemSettingsConfig.getLimitedItemWeight());
            if (scalableItemsExist) weightedProbability.put("scalable", ItemSettingsConfig.getScalableItemWeight());
        }

        String selectedLootSystem = pickWeighedProbability(weightedProbability);

        switch (selectedLootSystem) {
            case "procedural":
                return dropProcedurallyGeneratedItem(itemLevel, location, player);
            case "weighed":
                return dropWeighedFixedItem(location, player);
            case "fixed":
                return dropFixedItem(location, itemLevel, player);
            case "limited":
                return dropLimitedItem(location, itemLevel, player);
            case "scalable":
                return dropScalableItem(location, itemLevel, player);
        }

        return null;

    }

    public static ItemStack generateItemStack(int itemTier, Player player, EliteEntity eliteEntity) {

        HashMap<String, Double> weightedProbability = new HashMap<>();
        if (proceduralItemsOn) weightedProbability.put("procedural", ItemSettingsConfig.getProceduralItemWeight());
        if (customItemsOn) {
            if (weighedItemsExist) weightedProbability.put("weighed", ItemSettingsConfig.getWeighedItemWeight());
            if (fixedItemsExist) if (CustomItem.getFixedItems().containsKey(itemTier))
                weightedProbability.put("fixed", ItemSettingsConfig.getFixedItemWeight());
            if (limitedItemsExist) weightedProbability.put("limited", ItemSettingsConfig.getLimitedItemWeight());
            if (scalableItemsExist) weightedProbability.put("scalable", ItemSettingsConfig.getScalableItemWeight());
        }

        String selectedLootSystem = pickWeighedProbability(weightedProbability);

        switch (selectedLootSystem) {
            case "procedural":
                return generateProcedurallyGeneratedItem(itemTier, player, eliteEntity);
            case "weighed":
                return generateWeighedFixedItemStack(player);
            case "fixed":
                return generateFixedItem(itemTier, player, eliteEntity);
            case "limited":
                return generateLimitedItem(itemTier, player, eliteEntity);
            case "scalable":
                return generateScalableItem(itemTier, player, eliteEntity);
        }

        return null;

    }

    public static double setItemTier(int mobTier) {

        double chanceToUpgradeTier = 10 / (double) mobTier * ItemSettingsConfig.getMaximumItemLevel();

        if (ThreadLocalRandom.current().nextDouble() * 100 < chanceToUpgradeTier) return mobTier + 1D;


        double diceRoll = ThreadLocalRandom.current().nextDouble();

        /*
        10% of the time, give an item a tier below what the player is wearing
        40% of the time, give an item of the same tier as what the player is wearing
        50% of the time, give an item better than what the player is wearing
        If you're wondering why this isn't configurable, wonder instead why no one has noticed it isn't before you reading this
         */
        if (diceRoll < 0.10) mobTier -= 2;
        else if (diceRoll < 0.50) mobTier -= 1;

        if (mobTier < 0) mobTier = 0;

        return mobTier;

    }

    public static ItemStack dropWeighedFixedItem(EliteEntity eliteEntity, Player player) {
        ItemStack itemStack = generateWeighedFixedItemStack(player);
        if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) player.getInventory().addItem(itemStack);
        else processPhysicalItem(eliteEntity.getLocation(), itemStack, player);
        return itemStack;
    }


    public static ItemStack dropWeighedFixedItem(Location location, Player player) {
        ItemStack itemStack = generateWeighedFixedItemStack(player);
        if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) player.getInventory().addItem(itemStack);
        else processPhysicalItem(location, itemStack, player);
        return itemStack;
    }

    private static ItemStack generateWeighedFixedItemStack(Player player) {
        double totalWeight = 0;

        for (ItemStack itemStack : CustomItem.getWeighedFixedItems().keySet()) {
            Double shouldntBeNull = CustomItem.getWeighedFixedItems().get(itemStack);
            if (shouldntBeNull != null) totalWeight += CustomItem.getWeighedFixedItems().get(itemStack);
            else Logger.warn("Item " + itemStack.getItemMeta().getDisplayName() + " reported a null weight!");
        }

        ItemStack generatedItemStack = null;
        double random = Math.random() * totalWeight;

        for (ItemStack itemStack : CustomItem.getWeighedFixedItems().keySet()) {
            random -= CustomItem.getWeighedFixedItems().get(itemStack);
            if (random <= 0) {
                generatedItemStack = itemStack.clone();
                break;
            }
        }

        SoulbindEnchantment.addEnchantment(generatedItemStack, player);
        return generatedItemStack;
    }

    private static ItemStack dropProcedurallyGeneratedItem(int itemLevel, EliteEntity eliteEntity, Player player) {
        ItemStack itemStack = generateProcedurallyGeneratedItem(itemLevel, player, eliteEntity);
        if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) player.getInventory().addItem(itemStack);
        else processPhysicalItem(eliteEntity.getLocation(), itemStack, player);
        return itemStack;
    }

    private static ItemStack dropProcedurallyGeneratedItem(int itemLevel, Location location, Player player) {
        ItemStack itemStack = generateProcedurallyGeneratedItem(itemLevel, player, null);
        if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) player.getInventory().addItem(itemStack);
        else processPhysicalItem(location, itemStack, player);
        return itemStack;
    }

    private static ItemStack generateProcedurallyGeneratedItem(int itemLevel, Player player, EliteEntity eliteEntity) {
        return ItemConstructor.constructItem(itemLevel, eliteEntity, player, false);
    }

    private static ItemStack dropScalableItem(EliteEntity eliteEntity, int itemLevel, Player player) {
        ItemStack itemStack = generateScalableItem(itemLevel, player, eliteEntity);
        if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) player.getInventory().addItem(itemStack);
        else processPhysicalItem(eliteEntity.getLocation(), itemStack, player);
        return itemStack;
    }

    private static ItemStack dropScalableItem(Location location, int itemLevel, Player player) {
        ItemStack itemStack = generateScalableItem(itemLevel, player, null);
        if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) player.getInventory().addItem(itemStack);
        else processPhysicalItem(location, itemStack, player);
        return itemStack;
    }

    private static ItemStack generateScalableItem(int itemTier, Player player, EliteEntity eliteEntity) {
        return ScalableItemConstructor.randomizeScalableItem(itemTier, player, eliteEntity);
    }

    private static ItemStack dropLimitedItem(EliteEntity eliteEntity, int itemLevel, Player player) {
        ItemStack itemStack = generateLimitedItem(itemLevel, player, eliteEntity);
        if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) player.getInventory().addItem(itemStack);
        else processPhysicalItem(eliteEntity.getLocation(), itemStack, player);
        return itemStack;
    }

    private static ItemStack dropLimitedItem(Location location, int itemTier, Player player) {
        ItemStack itemStack = generateLimitedItem(itemTier, player, null);
        if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) player.getInventory().addItem(itemStack);
        else processPhysicalItem(location, itemStack, player);
        return itemStack;
    }

    private static ItemStack generateLimitedItem(int itemTier, Player player, EliteEntity eliteEntity) {
        return ScalableItemConstructor.randomizeLimitedItem(itemTier, player, eliteEntity);
    }

    private static ItemStack dropFixedItem(EliteEntity eliteEntity, int itemTier, Player player) {
        ItemStack itemStack = generateFixedItem(itemTier, player, eliteEntity);
        if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) player.getInventory().addItem(itemStack);
        else processPhysicalItem(eliteEntity.getLocation(), itemStack, player);
        return itemStack;
    }

    private static ItemStack dropFixedItem(Location location, int itemTier, Player player) {
        ItemStack itemStack = generateFixedItem(itemTier, player, null);
        if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) player.getInventory().addItem(itemStack);
        else processPhysicalItem(location, itemStack, player);
        return itemStack;
    }

    private static ItemStack generateFixedItem(int itemTier, Player player, EliteEntity eliteEntity) {
        return CustomItem.getFixedItems().get(itemTier).get(ThreadLocalRandom.current().nextInt(CustomItem.getFixedItems().get(itemTier).size())).generateDefaultsItemStack(player, false, eliteEntity);
    }

    private static void processPhysicalItem(Location location, ItemStack itemStack, Player player) {
        Item item = location.getWorld().dropItem(location, itemStack);
        if (item.getItemStack().hasItemMeta() && item.getItemStack().getItemMeta().hasDisplayName()) {
            item.setCustomName(item.getItemStack().getItemMeta().getDisplayName());
            item.setCustomNameVisible(true);
        }
        SoulbindEnchantment.addPhysicalDisplay(item, player);
        RareDropEffect.runEffect(item);
    }

    public static void generateSpecialLoot(Player player, int level, EliteEntity eliteEntity) {
        CustomItem customItem = WeightedProbability.pickWeighedProbabilityFromCustomItems(SpecialItemSystemsConfig.getSpecialValues());
        if (customItem == null) return;
        player.getWorld().dropItem(player.getLocation(), customItem.generateItemStack(level, player, eliteEntity));
    }


    @EventHandler
    public void onDeath(EliteMobDeathEvent event) {
        if (event.getEntityDeathEvent() != null && !event.getEliteEntity().isVanillaLoot())
            event.getEntityDeathEvent().getDrops().clear();
        if (!event.getEliteEntity().isEliteLoot()) return;
        if (event.getEliteEntity().getLevel() < 1) return;
        if (event.getEliteEntity().getDamagers().isEmpty()) return;

        generatePlayerLoot(event.getEliteEntity());
    }

}
