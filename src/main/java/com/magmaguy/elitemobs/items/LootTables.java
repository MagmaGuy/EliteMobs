package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.ItemsDropSettingsConfig;
import com.magmaguy.elitemobs.config.ItemsProceduralSettingsConfig;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import static com.magmaguy.elitemobs.utils.WeightedProbability.pickWeighedProbability;

/**
 * Created by MagmaGuy on 04/06/2017.
 */
public class LootTables implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(EliteMobDeathEvent event) {

        if (!event.getEliteMobEntity().getHasSpecialLoot()) return;
        if (event.getEliteMobEntity().getLevel() < 2) return;
        if (event.getEliteMobEntity().getDamagers().isEmpty()) return;

        ItemLootShower.runShower(event.getEliteMobEntity().getTier(), event.getEliteMobEntity().getLivingEntity().getLocation());
        Item item = generateLoot(event.getEliteMobEntity());

        if (item == null) return;

        RareDropEffect.runEffect(item);

    }

    private static boolean proceduralItemsOn = ConfigValues.itemsProceduralSettingsConfig.getBoolean(ItemsProceduralSettingsConfig.DROP_ITEMS_ON_DEATH);
    private static boolean customItemsOn = ConfigValues.itemsDropSettingsConfig.getBoolean(ItemsDropSettingsConfig.DROP_CUSTOM_ITEMS) &&
            !CustomItem.getCustomItemStackList().isEmpty();
    private static boolean weighedItemsExist = CustomItem.getWeighedFixedItems() != null && !CustomItem.getWeighedFixedItems().isEmpty();
    private static boolean fixedItemsExist = CustomItem.getFixedItems() != null && !CustomItem.getFixedItems().isEmpty();
    private static boolean limitedItemsExist = CustomItem.getLimitedItem() != null && !CustomItem.getLimitedItem().isEmpty();
    private static boolean scalableItemsExist = CustomItem.getScalableItems() != null && !CustomItem.getScalableItems().isEmpty();

    public static Item generateLoot(EliteMobEntity eliteMobEntity) {

        int mobTier = (int) MobTierCalculator.findMobTier(eliteMobEntity);

        /*
        Add some wiggle room to avoid making obtaining loot too linear
         */
        int itemTier = (int) setItemTier(mobTier);

         /*
        Handle the odds of an item dropping
         */
        double baseChance = ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.ELITE_ITEM_FLAT_DROP_RATE) / 100;
        double dropChanceBonus = ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.ELITE_ITEM_TIER_DROP_RATE) / 100 * itemTier;

        if (ThreadLocalRandom.current().nextDouble() > baseChance + dropChanceBonus)
            return null;

        HashMap<String, Double> weightedProbability = new HashMap<>();
        if (proceduralItemsOn)
            weightedProbability.put("procedural", ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.PROCEDURAL_ITEM_WEIGHT));
        if (customItemsOn) {
            if (weighedItemsExist)
                weightedProbability.put("weighed", ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.WEIGHED_ITEM_WEIGHT));
            if (fixedItemsExist)
                if (CustomItem.getFixedItems().keySet().contains(itemTier))
                    weightedProbability.put("fixed", ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.FIXED_ITEM_WEIGHT));
            if (limitedItemsExist)
                weightedProbability.put("limited", ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.LIMITED_ITEM_WEIGHT));
            if (scalableItemsExist)
                weightedProbability.put("scalable", ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.SCALABLE_ITEM_WEIGHT));
        }

        String selectedLootSystem = pickWeighedProbability(weightedProbability);

        switch (selectedLootSystem) {
            case "procedural":
                return dropProcedurallyGeneratedItem(itemTier, eliteMobEntity);
            case "weighed":
                return dropWeighedFixedItem(eliteMobEntity.getLivingEntity().getLocation());
            case "fixed":
                return dropFixedItem(eliteMobEntity, itemTier);
            case "limited":
                return dropLimitedItem(eliteMobEntity, itemTier);
            case "scalable":
                return dropScalableItem(eliteMobEntity, itemTier);
        }

        return null;

    }

    public static double setItemTier(int mobTier) {

        double chanceToUpgradeTier = 10 / (double) mobTier * ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.MAXIMUM_LOOT_TIER);

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

    private static Item dropWeighedFixedItem(Location location) {

        double totalWeight = 0;

        for (ItemStack itemStack : CustomItem.getWeighedFixedItems().keySet())
            totalWeight += CustomItem.getWeighedFixedItems().get(itemStack);

        ItemStack generatedItemStack = null;
        double random = Math.random() * totalWeight;

        for (ItemStack itemStack : CustomItem.getWeighedFixedItems().keySet()) {
            random -= CustomItem.getWeighedFixedItems().get(itemStack);
            if (random <= 0) {
                generatedItemStack = itemStack;
                break;
            }
        }

        return location.getWorld().dropItem(location, generatedItemStack);

    }

    private static Item dropProcedurallyGeneratedItem(int tierLevel, EliteMobEntity eliteMobEntity) {

        ItemStack randomLoot = ItemConstructor.constructItem(tierLevel, eliteMobEntity);
        return eliteMobEntity.getLivingEntity().getWorld().dropItem(eliteMobEntity.getLivingEntity().getLocation(), randomLoot);

    }

    private static Item dropScalableItem(EliteMobEntity eliteMobEntity, int itemTier) {

        return eliteMobEntity.getLivingEntity().getWorld().dropItem(eliteMobEntity.getLivingEntity().getLocation(),
                ScalableItemConstructor.randomizeScalableItem(itemTier));

    }

    private static Item dropLimitedItem(EliteMobEntity eliteMobEntity, int itemTier) {

        return eliteMobEntity.getLivingEntity().getWorld().dropItem(eliteMobEntity.getLivingEntity().getLocation(),
                ScalableItemConstructor.randomizeLimitedItem(itemTier));

    }

    private static Item dropFixedItem(EliteMobEntity eliteMobEntity, int itemTier) {

        return eliteMobEntity.getLivingEntity().getWorld().dropItem(eliteMobEntity.getLivingEntity().getLocation(),
                CustomItem.getFixedItems().get(itemTier).get(ThreadLocalRandom.current().nextInt(CustomItem.getFixedItems().get(itemTier).size())).generateDefaultsItemStack());

    }

}
