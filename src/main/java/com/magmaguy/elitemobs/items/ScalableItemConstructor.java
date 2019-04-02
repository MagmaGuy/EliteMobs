package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.ItemsDropSettingsConfig;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.items.itemconstructor.ScalableItemObject;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ScalableItemConstructor {

    public static List<ScalableItemObject> dynamicallyScalableItems = new ArrayList<>();
    public static HashMap<Integer, List<ScalableItemObject>> limitedScalableItems = new HashMap<>();
    public static HashMap<Integer, List<ItemStack>> staticItems = new HashMap<>();

    /*
    Fully dynamic scalable items can be of any item rank all the way up to the maximum valid item tier.
    As such, there is no sense in trying to sort this by tiers.
     */
    public static ItemStack assembleDynamicItems(int itemTier) {

        ScalableItemObject scalableItemObject = dynamicallyScalableItems.get(ThreadLocalRandom.current().nextInt(dynamicallyScalableItems.size()));

        return constructDynamicItem(itemTier, scalableItemObject);

    }

    public static ItemStack constructDynamicItem(int itemTier, ScalableItemObject scalableItemObject) {

        HashMap<Enchantment, Integer> newEnchantmentList = updateDynamicEnchantments(scalableItemObject.enchantments, itemTier);
        return ItemConstructor.constructItem(
                scalableItemObject.rawName,
                scalableItemObject.material,
                newEnchantmentList,
                scalableItemObject.customEnchantments,
                scalableItemObject.potionEffects,
                scalableItemObject.lore
        );

    }

    private static HashMap<Enchantment, Integer> updateDynamicEnchantments(HashMap<Enchantment, Integer> enchantmentsList, int itemTier) {

        if (enchantmentsList.containsKey(Enchantment.PROTECTION_ENVIRONMENTAL) &&
                enchantmentsList.get(Enchantment.PROTECTION_ENVIRONMENTAL) >
                        ConfigValues.itemsDropSettingsConfig.getInt(ItemsDropSettingsConfig.MAXIMUM_LOOT_TIER))
            itemTier = ConfigValues.itemsDropSettingsConfig.getInt(ItemsDropSettingsConfig.MAXIMUM_LOOT_TIER);

        if (enchantmentsList.containsKey(Enchantment.DAMAGE_ALL) &&
                enchantmentsList.get(Enchantment.DAMAGE_ALL) >
                        ConfigValues.itemsDropSettingsConfig.getInt(ItemsDropSettingsConfig.MAXIMUM_LOOT_TIER))
            itemTier = ConfigValues.itemsDropSettingsConfig.getInt(ItemsDropSettingsConfig.MAXIMUM_LOOT_TIER);

        if (enchantmentsList.containsKey(Enchantment.ARROW_DAMAGE) &&
                enchantmentsList.get(Enchantment.ARROW_DAMAGE) >
                        ConfigValues.itemsDropSettingsConfig.getInt(ItemsDropSettingsConfig.MAXIMUM_LOOT_TIER))
            itemTier = ConfigValues.itemsDropSettingsConfig.getInt(ItemsDropSettingsConfig.MAXIMUM_LOOT_TIER);

        HashMap<Enchantment, Integer> newEnchantmentList = new HashMap<>();
        HashMap<Enchantment, Integer> secondaryEnchantmentList = new HashMap<>();

        /*
        Small limitation of this system, it doesn't take into account the material tier and just defaults to assume it's
        diamond (hence the -1 enchantment).
        It's such a small thing that I don't feel it's worth the hassle of adjusting it.
         */
        for (Enchantment enchantment : enchantmentsList.keySet())
            if (enchantment.equals(Enchantment.DAMAGE_ALL) ||
                    enchantment.equals(Enchantment.ARROW_DAMAGE) ||
                    enchantment.equals(Enchantment.PROTECTION_ENVIRONMENTAL))
                newEnchantmentList.put(enchantment, itemTier - 1);
            else
                secondaryEnchantmentList.put(enchantment, enchantmentsList.get(enchantment));

        if (itemTier < 2) return newEnchantmentList;

        int secondaryEnchantmentPool = ThreadLocalRandom.current().nextInt((int) Math.ceil((double) itemTier / 2));

        for (int i = 0; i < secondaryEnchantmentPool; i++) {

            if (secondaryEnchantmentList.isEmpty()) break;

            int randomIndex = ThreadLocalRandom.current().nextInt(secondaryEnchantmentList.size());
            Enchantment randomizedEnchantment = (Enchantment) secondaryEnchantmentList.keySet().toArray()[randomIndex];

            if (newEnchantmentList.isEmpty() || !newEnchantmentList.containsKey(randomizedEnchantment)) {
                newEnchantmentList.put(randomizedEnchantment, 1);
            } else {
                int newEnchantmentLevel = newEnchantmentList.get(randomizedEnchantment) + 1;
                newEnchantmentList.put(randomizedEnchantment, newEnchantmentLevel);
            }

            int leftoverPoolEnchantment = secondaryEnchantmentList.get(randomizedEnchantment) - 1;
            if (leftoverPoolEnchantment <= 0)
                secondaryEnchantmentList.remove(randomizedEnchantment);
            else
                secondaryEnchantmentList.put(randomizedEnchantment, leftoverPoolEnchantment);

        }


        return newEnchantmentList;

    }

    /*
    Limited scalable items have the item in the config as the best possible item that the plugin will generate for that
    entry, and every other entry is just a nerfed version of that item. Basically an easy way to limit an item in a
    predictable way.
     */
    public static ItemStack assembleLimitedItem(int itemTier) {

        List<ScalableItemObject> localLootList = new ArrayList<>();

        for (int i = 0; i < itemTier; i++)
            if (limitedScalableItems.containsKey(i))
                localLootList.addAll(limitedScalableItems.get(i));


        ScalableItemObject scalableItemObject = localLootList.get(ThreadLocalRandom.current().nextInt(localLootList.size()) - 1);

        return constructLimitedItem(itemTier, scalableItemObject);

    }

    public static ItemStack constructLimitedItem(int itemTier, ScalableItemObject scalableItemObject) {

        HashMap<Enchantment, Integer> newEnchantmentList = updateLimitedEnchantments(scalableItemObject.enchantments, itemTier);

        return ItemConstructor.constructItem(
                scalableItemObject.rawName,
                scalableItemObject.material,
                newEnchantmentList,
                scalableItemObject.customEnchantments,
                scalableItemObject.potionEffects,
                scalableItemObject.lore
        );

    }

    private static HashMap<Enchantment, Integer> updateLimitedEnchantments(HashMap<Enchantment, Integer> enchantmentsList,
                                                                           int itemTier) {
        if (enchantmentsList.containsKey(Enchantment.PROTECTION_ENVIRONMENTAL) &&
                enchantmentsList.get(Enchantment.PROTECTION_ENVIRONMENTAL) < itemTier)
            itemTier = enchantmentsList.get(Enchantment.PROTECTION_ENVIRONMENTAL);

        if (enchantmentsList.containsKey(Enchantment.DAMAGE_ALL) &&
                enchantmentsList.get(Enchantment.DAMAGE_ALL) >
                        ConfigValues.itemsDropSettingsConfig.getInt(ItemsDropSettingsConfig.MAXIMUM_LOOT_TIER))
            itemTier = enchantmentsList.get(Enchantment.DAMAGE_ALL);

        if (enchantmentsList.containsKey(Enchantment.ARROW_DAMAGE) &&
                enchantmentsList.get(Enchantment.ARROW_DAMAGE) >
                        ConfigValues.itemsDropSettingsConfig.getInt(ItemsDropSettingsConfig.MAXIMUM_LOOT_TIER))
            itemTier = enchantmentsList.get(Enchantment.ARROW_DAMAGE);


        return updateDynamicEnchantments(enchantmentsList, itemTier);

    }


}
