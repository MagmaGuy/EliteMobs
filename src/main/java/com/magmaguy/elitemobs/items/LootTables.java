package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.ProceduralItemGenerationSettingsConfig;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
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
        if (eliteEntity.isTriggeredAntiExploit())
            return;
        for (Player player : eliteEntity.getDamagers().keySet()) {

            if (eliteEntity.getDamagers().get(player) / eliteEntity.getMaxHealth() < 0.1)
                continue;

            if (eliteEntity.getPower("bonus_coins.yml") == null)
                new ItemLootShower(eliteEntity.getLevel(), eliteEntity.getUnsyncedLivingEntity().getLocation(), player);

            Item item = null;

            if (AdventurersGuildConfig.guildLootLimiter) {
                double itemTier = setItemTier(eliteEntity.getLevel());
                if (itemTier > GuildRank.getActiveGuildRank(player) * 10) {
                    itemTier = GuildRank.getActiveGuildRank(player) * 10;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(AdventurersGuildConfig.lootLimiterMessage));
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 20 * 10);
                }
                item = generateLoot((int) Math.floor(itemTier), eliteEntity, player);
            } else
                item = generateLoot(eliteEntity, player);

            if (item != null &&
                    item.getItemStack() != null &&
                    item.getItemStack().hasItemMeta() &&
                    item.getItemStack().getItemMeta().hasDisplayName()) {
                item.setCustomName(item.getItemStack().getItemMeta().getDisplayName());
                item.setCustomNameVisible(true);
            }

            SoulbindEnchantment.addPhysicalDisplay(item, player);

            if (item == null) continue;

            RareDropEffect.runEffect(item);
        }
    }

    public static void initialize() {
        proceduralItemsOn = ProceduralItemGenerationSettingsConfig.doProceduralItemDrops;
        customItemsOn = ItemSettingsConfig.isDoEliteMobsLoot() && !CustomItem.getCustomItemStackList().isEmpty();
        weighedItemsExist = CustomItem.getWeighedFixedItems() != null && !CustomItem.getWeighedFixedItems().isEmpty();
        fixedItemsExist = CustomItem.getFixedItems() != null && !CustomItem.getFixedItems().isEmpty();
        limitedItemsExist = CustomItem.getLimitedItem() != null && !CustomItem.getLimitedItem().isEmpty();
        scalableItemsExist = CustomItem.getScalableItems() != null && !CustomItem.getScalableItems().isEmpty();
    }

    private static Item generateLoot(EliteEntity eliteEntity, Player player) {

        int mobTier = (int) MobTierCalculator.findMobTier(eliteEntity);

        /*
        Add some wiggle room to avoid making obtaining loot too linear
         */
        int itemTier = (int) setItemTier(mobTier);

        return generateLoot(itemTier, eliteEntity, player);

    }

    public static Item generateLoot(int itemTier, EliteEntity eliteEntity, Player player) {

         /*
        Handle the odds of an item dropping
         */
        double baseChance = ItemSettingsConfig.getFlatDropRate();
        double dropChanceBonus = ItemSettingsConfig.getTierIncreaseDropRate() * itemTier;

        if (ThreadLocalRandom.current().nextDouble() > baseChance + dropChanceBonus)
            return null;

        HashMap<String, Double> weightedProbability = new HashMap<>();
        if (proceduralItemsOn)
            weightedProbability.put("procedural", ItemSettingsConfig.getProceduralItemWeight());
        if (customItemsOn) {
            if (weighedItemsExist)
                weightedProbability.put("weighed", ItemSettingsConfig.getWeighedItemWeight());
            if (fixedItemsExist)
                if (CustomItem.getFixedItems().containsKey(itemTier))
                    weightedProbability.put("fixed", ItemSettingsConfig.getFixedItemWeight());
            if (limitedItemsExist)
                weightedProbability.put("limited", ItemSettingsConfig.getLimitedItemWeight());
            if (scalableItemsExist)
                weightedProbability.put("scalable", ItemSettingsConfig.getScalableItemWeight());
        }

        String selectedLootSystem = pickWeighedProbability(weightedProbability);

        if (selectedLootSystem == null) {
            new InfoMessage("Your EliteMobs loot configuration resulted in no loot getting dropped. This is not a bug. " +
                    "If you want players to be able to progress at all in the EliteMobs plugin, review your configuration settings.");
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
     * @param itemTier
     * @param location
     * @param player
     * @return
     */
    public static Item generateLoot(int itemTier, Location location, Player player) {

         /*
        Handle the odds of an item dropping
         */
        double baseChance = ItemSettingsConfig.getFlatDropRate();
        double dropChanceBonus = ItemSettingsConfig.getTierIncreaseDropRate() * itemTier;

        if (ThreadLocalRandom.current().nextDouble() > baseChance + dropChanceBonus)
            return null;

        HashMap<String, Double> weightedProbability = new HashMap<>();
        if (proceduralItemsOn)
            weightedProbability.put("procedural", ItemSettingsConfig.getProceduralItemWeight());
        if (customItemsOn) {
            if (weighedItemsExist)
                weightedProbability.put("weighed", ItemSettingsConfig.getWeighedItemWeight());
            if (fixedItemsExist)
                if (CustomItem.getFixedItems().containsKey(itemTier))
                    weightedProbability.put("fixed", ItemSettingsConfig.getFixedItemWeight());
            if (limitedItemsExist)
                weightedProbability.put("limited", ItemSettingsConfig.getLimitedItemWeight());
            if (scalableItemsExist)
                weightedProbability.put("scalable", ItemSettingsConfig.getScalableItemWeight());
        }

        String selectedLootSystem = pickWeighedProbability(weightedProbability);

        switch (selectedLootSystem) {
            case "procedural":
                return dropProcedurallyGeneratedItem(itemTier, location, player);
            case "weighed":
                return dropWeighedFixedItem(location, player);
            case "fixed":
                return dropFixedItem(location, itemTier, player);
            case "limited":
                return dropLimitedItem(location, itemTier, player);
            case "scalable":
                return dropScalableItem(location, itemTier, player);
        }

        return null;

    }

    public static ItemStack generateItemStack(int itemTier, Player player, EliteEntity eliteEntity) {

        HashMap<String, Double> weightedProbability = new HashMap<>();
        if (proceduralItemsOn)
            weightedProbability.put("procedural", ItemSettingsConfig.getProceduralItemWeight());
        if (customItemsOn) {
            if (weighedItemsExist)
                weightedProbability.put("weighed", ItemSettingsConfig.getWeighedItemWeight());
            if (fixedItemsExist)
                if (CustomItem.getFixedItems().containsKey(itemTier))
                    weightedProbability.put("fixed", ItemSettingsConfig.getFixedItemWeight());
            if (limitedItemsExist)
                weightedProbability.put("limited", ItemSettingsConfig.getLimitedItemWeight());
            if (scalableItemsExist)
                weightedProbability.put("scalable", ItemSettingsConfig.getScalableItemWeight());
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

        double chanceToUpgradeTier = 10 / (double) mobTier * ItemSettingsConfig.getMaximumLootTier();

        if (ThreadLocalRandom.current().nextDouble() * 100 < chanceToUpgradeTier)
            return mobTier + 1;


        double diceRoll = ThreadLocalRandom.current().nextDouble();

        /*
        10% of the time, give an item a tier below what the player is wearing
        40% of the time, give an item of the same tier as what the player is wearing
        50% of the time, give an item better than what the player is wearing
        If you're wondering why this isn't configurable, wonder instead why no one has noticed it isn't before you reading this
         */
        if (diceRoll < 0.10)
            mobTier -= 2;
        else if (diceRoll < 0.50)
            mobTier -= 1;

        if (mobTier < 0) mobTier = 0;

        return mobTier;

    }

    public static Item dropWeighedFixedItem(EliteEntity eliteEntity, Player player) {
        return eliteEntity.getUnsyncedLivingEntity().getLocation().getWorld().dropItem(eliteEntity.getUnsyncedLivingEntity().getLocation(), generateWeighedFixedItemStack(player));
    }


    public static Item dropWeighedFixedItem(Location location, Player player) {
        return location.getWorld().dropItem(location, generateWeighedFixedItemStack(player));
    }

    private static ItemStack generateWeighedFixedItemStack(Player player) {
        double totalWeight = 0;

        for (ItemStack itemStack : CustomItem.getWeighedFixedItems().keySet()) {
            Double shouldntBeNull = CustomItem.getWeighedFixedItems().get(itemStack);
            if (shouldntBeNull != null)
                totalWeight += CustomItem.getWeighedFixedItems().get(itemStack);
            else
                new WarningMessage("Item " + itemStack.getItemMeta().getDisplayName() + " reported a null weight!");
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

    private static Item dropProcedurallyGeneratedItem(int tierLevel, EliteEntity eliteEntity, Player player) {
        return eliteEntity.getUnsyncedLivingEntity().getWorld().dropItem(eliteEntity.getUnsyncedLivingEntity().getLocation(),
                generateProcedurallyGeneratedItem(tierLevel, player, eliteEntity));
    }

    private static Item dropProcedurallyGeneratedItem(int tierLevel, Location location, Player player) {
        return location.getWorld().dropItem(location, generateProcedurallyGeneratedItem(tierLevel, player, null));
    }

    private static ItemStack generateProcedurallyGeneratedItem(int tierLevel, Player player, EliteEntity eliteEntity) {
        return ItemConstructor.constructItem(tierLevel, eliteEntity, player, false);
    }

    private static Item dropScalableItem(EliteEntity eliteEntity, int itemTier, Player player) {
        return eliteEntity.getUnsyncedLivingEntity().getWorld().dropItem(eliteEntity.getUnsyncedLivingEntity().getLocation(),
                generateScalableItem(itemTier, player, eliteEntity));
    }

    private static Item dropScalableItem(Location location, int itemTier, Player player) {
        return location.getWorld().dropItem(location, generateScalableItem(itemTier, player, null));
    }

    private static ItemStack generateScalableItem(int itemTier, Player player, EliteEntity eliteEntity) {
        return ScalableItemConstructor.randomizeScalableItem(itemTier, player, eliteEntity);
    }

    private static Item dropLimitedItem(EliteEntity eliteEntity, int itemTier, Player player) {
        return eliteEntity.getUnsyncedLivingEntity().getWorld().dropItem(eliteEntity.getUnsyncedLivingEntity().getLocation(),
                generateLimitedItem(itemTier, player, eliteEntity));
    }

    private static Item dropLimitedItem(Location location, int itemTier, Player player) {
        return location.getWorld().dropItem(location, generateLimitedItem(itemTier, player, null));
    }

    private static ItemStack generateLimitedItem(int itemTier, Player player, EliteEntity eliteEntity) {
        return ScalableItemConstructor.randomizeLimitedItem(itemTier, player, eliteEntity);
    }

    private static Item dropFixedItem(EliteEntity eliteEntity, int itemTier, Player player) {
        return eliteEntity.getUnsyncedLivingEntity().getWorld().dropItem(
                eliteEntity.getUnsyncedLivingEntity().getLocation(), generateFixedItem(itemTier, player, eliteEntity));
    }

    private static Item dropFixedItem(Location location, int itemTier, Player player) {
        return location.getWorld().dropItem(location, generateFixedItem(itemTier, player, null));
    }

    private static ItemStack generateFixedItem(int itemTier, Player player, EliteEntity eliteEntity) {
        return CustomItem.getFixedItems().get(itemTier)
                .get(ThreadLocalRandom.current().nextInt(CustomItem.getFixedItems().get(itemTier).size()))
                .generateDefaultsItemStack(player, false, eliteEntity);
    }


    @EventHandler
    public void onDeath(EliteMobDeathEvent event) {
        if (!event.getEliteEntity().isVanillaLoot())
            event.getEntityDeathEvent().getDrops().clear();
        if (!event.getEliteEntity().isEliteLoot()) return;
        if (event.getEliteEntity().getLevel() < 1) return;
        if (event.getEliteEntity().getDamagers().isEmpty()) return;

        generatePlayerLoot(event.getEliteEntity());
    }

}
