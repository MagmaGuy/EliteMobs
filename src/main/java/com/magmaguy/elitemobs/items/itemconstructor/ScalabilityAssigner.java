package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.items.ItemTierFinder;
import com.magmaguy.elitemobs.items.ScalableItemConstructor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ScalabilityAssigner {

    public static void assign(ItemStack itemStack, String rawName, Material material, HashMap<Enchantment,
            Integer> enchantments, HashMap<String, Integer> customEnchantments, List<String> potionEffects,
                              List<String> lore, String dropType, String scalabilityType) {

        /*
        For the sake of clarity for the users and just overall reliability scalability will only be assigned to dynamic
        items, and all static items will be considered statically scalable
         */
        if (!DropWeightHandler.isDynamic(dropType)) return;
        if (!isScalable(scalabilityType)) return;

        ScalableItemObject scalableItemObject = new ScalableItemObject();
        scalableItemObject.initializeItemObject(rawName, material, enchantments, customEnchantments, potionEffects,
                lore);

        if (scalabilityType == null || scalabilityType.equalsIgnoreCase("dynamic")) {
            ScalableItemConstructor.dynamicallyScalableItems.add(scalableItemObject);
            return;
        }

        if (scalabilityType.equalsIgnoreCase("limited")) {

            int itemTier = (int) ItemTierFinder.findBattleTier(itemStack);

            if (!ScalableItemConstructor.limitedScalableItems.containsKey(itemTier))
                ScalableItemConstructor.limitedScalableItems.put((int) ItemTierFinder.findBattleTier(itemStack), new ArrayList<>(Arrays.asList(scalableItemObject)));
            else {
                List<ScalableItemObject> existingList = ScalableItemConstructor.limitedScalableItems.get(itemTier);
                existingList.add(scalableItemObject);
                ScalableItemConstructor.limitedScalableItems.put((int) ItemTierFinder.findBattleTier(itemStack), existingList);
            }

        }

        if (scalabilityType.equalsIgnoreCase("static")) {

            int itemTier = (int) ItemTierFinder.findBattleTier(itemStack);

            if (!ScalableItemConstructor.staticItems.containsKey(itemTier))
                ScalableItemConstructor.staticItems.put((int) ItemTierFinder.findBattleTier(itemStack), new ArrayList<>(Arrays.asList(itemStack)));
            else {
                List<ItemStack> existingList = ScalableItemConstructor.staticItems.get(itemTier);
                existingList.add(itemStack);
                ScalableItemConstructor.staticItems.put((int) ItemTierFinder.findBattleTier(itemStack), existingList);
            }

        }

    }

    public static boolean isScalable(String scalabilityType) {

        return scalabilityType == null || scalabilityType.equalsIgnoreCase("dynamic") || scalabilityType.equalsIgnoreCase("dynamic_full");

    }

}
