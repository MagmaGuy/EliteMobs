package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.ItemsDropSettingsConfig;
import com.magmaguy.elitemobs.config.ItemsProceduralSettingsConfig;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import static com.magmaguy.elitemobs.utils.WeightedProbablity.pickWeighedProbability;

/**
 * Created by MagmaGuy on 04/06/2017.
 */
public class LootTables implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(EntityDeathEvent event) {

        if (event.getEntity() == null) return;

        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
        if (eliteMobEntity == null) return;

        if (!eliteMobEntity.isNaturalEntity()) return;
        if (!eliteMobEntity.getHasNormalLoot()) return;

        if (eliteMobEntity.getLevel() < 2) return;

        Item item = generateLoot(eliteMobEntity);

        if (item == null) return;

        RareDropEffect.runEffect(item);

    }

    private static boolean proceduralItemsOn = ConfigValues.itemsProceduralSettingsConfig.getBoolean(ItemsProceduralSettingsConfig.DROP_ITEMS_ON_DEATH);
    private static boolean customItemsOn = ConfigValues.itemsDropSettingsConfig.getBoolean(ItemsDropSettingsConfig.DROP_CUSTOM_ITEMS) &&
            !CustomItemConstructor.customItemList.isEmpty();
    private static boolean staticWeightCustomItemsExist = CustomItemConstructor.staticCustomItemHashMap.size() > 0;
    private static boolean dynamicWeightCustomItemsExist = CustomItemConstructor.dynamicRankedItemStacks.size() > 0;

    public static Item generateLoot(EliteMobEntity eliteMobEntity) {


        int mobTier = (int) MobTierFinder.findMobTier(eliteMobEntity);
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

        /*
        First split: Check if the loot should be procedural or based on unique/custom items
        This makes more sense because the ratio between custom/unique items and procedural items is quite drastic
         */
        if (!proceduralItemsOn && !customItemsOn) return null;
        if (proceduralItemsOn && customItemsOn) {

            HashMap<String, Double> weightedConfigValues = new HashMap<>();
            weightedConfigValues.put(ItemsDropSettingsConfig.PROCEDURAL_ITEM_WEIGHT, ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.PROCEDURAL_ITEM_WEIGHT));
            weightedConfigValues.put(ItemsDropSettingsConfig.CUSTOM_ITEM_WEIGHT, ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.CUSTOM_ITEM_WEIGHT));

            String selectedLootSystem = pickWeighedProbability(weightedConfigValues);

            if (selectedLootSystem.equals(ItemsDropSettingsConfig.PROCEDURAL_ITEM_WEIGHT))
                return dropProcedurallyGeneratedItem(itemTier, eliteMobEntity);


        }


        if (proceduralItemsOn && !customItemsOn)
            return dropProcedurallyGeneratedItem(itemTier, eliteMobEntity);



        /*
        First split is done, moving on to second split
        Split between static and dynamic weight loot
         */
        if (staticWeightCustomItemsExist && dynamicWeightCustomItemsExist) {

            HashMap<String, Double> weightedConfigValues = new HashMap<>();
            weightedConfigValues.put(ItemsDropSettingsConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT, ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT));
            weightedConfigValues.put(ItemsDropSettingsConfig.CUSTOM_STATIC_ITEM_WEIGHT, ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.CUSTOM_STATIC_ITEM_WEIGHT));

            String selectedLootSystem = pickWeighedProbability(weightedConfigValues);

            if (selectedLootSystem.equals(ItemsDropSettingsConfig.CUSTOM_STATIC_ITEM_WEIGHT)) {
                return dropCustomStaticLoot(eliteMobEntity.getLivingEntity().getLocation());

            }

        }

        /*
        Second split is done, moving on to third split
        At this point only scalability type is left, can be either static, limited or dynamic
         */
        HashMap<String, Double> weightedProbability = new HashMap<>();
        if (ScalableItemConstructor.dynamicallyScalableItems.size() > 0)
            weightedProbability.put("dynamic", 33.0);
        if (ScalableItemConstructor.limitedScalableItems.size() > 0)
            weightedProbability.put("limited", 33.0);
        if (ScalableItemConstructor.staticItems.size() > 0)
            weightedProbability.put("static", 33.0);

        String selectedLootSystem = pickWeighedProbability(weightedProbability);

        switch (selectedLootSystem) {
            case "dynamic":
                return dropDynamicallyScalingItem(eliteMobEntity, itemTier);
            case "limited":
                return dropLimitedScalingItem(eliteMobEntity, itemTier);
            case "static":
                return dropStaticScalingItem(eliteMobEntity, itemTier);
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

    private static Item dropCustomStaticLoot(Location location) {

        double totalWeight = 0;

        for (ItemStack itemStack : CustomItemConstructor.staticCustomItemHashMap.keySet())
            totalWeight += CustomItemConstructor.staticCustomItemHashMap.get(itemStack);


        ItemStack generatedItemStack = null;
        double random = Math.random() * totalWeight;

        for (ItemStack itemStack : CustomItemConstructor.staticCustomItemHashMap.keySet()) {

            random -= CustomItemConstructor.staticCustomItemHashMap.get(itemStack);

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

    private static Item dropDynamicallyScalingItem(EliteMobEntity eliteMobEntity, int itemTier) {

        return eliteMobEntity.getLivingEntity().getWorld().dropItem(eliteMobEntity.getLivingEntity().getLocation(),
                ScalableItemConstructor.constructDynamicItem(itemTier));

    }

    private static Item dropLimitedScalingItem(EliteMobEntity eliteMobEntity, int itemTier) {

        return eliteMobEntity.getLivingEntity().getWorld().dropItem(eliteMobEntity.getLivingEntity().getLocation(),
                ScalableItemConstructor.constructLimitedItem(itemTier));

    }

    private static Item dropStaticScalingItem(EliteMobEntity eliteMobEntity, int itemTier) {

        return eliteMobEntity.getLivingEntity().getWorld().dropItem(eliteMobEntity.getLivingEntity().getLocation(),
                ScalableItemConstructor.staticItems.get(itemTier).get(ThreadLocalRandom.current()
                        .nextInt(ScalableItemConstructor.staticItems.get(itemTier).size()) - 1));

    }

}
