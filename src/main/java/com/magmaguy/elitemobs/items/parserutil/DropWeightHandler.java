package com.magmaguy.elitemobs.items.parserutil;

import com.magmaguy.elitemobs.items.CustomItemConstructor;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import com.magmaguy.elitemobs.items.uniqueitems.UniqueItemInitializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;


public class DropWeightHandler {

    public static void process(String dropType, ItemStack itemStack) {

        if (dropType == null || dropType.isEmpty()) {

            rankedItemMapCreator(itemStack);
            return;

        }

        /*
        Code doesn't check for "dynamic" as it's the same behavior as nothing being in this key
         */
        if (dropType.equalsIgnoreCase("dynamic")) {

            rankedItemMapCreator(itemStack);
            return;

        }

        /*
        Unique items only ever drop from specific bosses
         */
        if (dropType.equalsIgnoreCase("unique")) {

            UniqueItemInitializer.uniqueItemsList.add(itemStack);
            return;

        }


        /*
        Static items can drop at any point
         */
        try {

            Double dropWeight = Double.valueOf(dropType);
            CustomItemConstructor.staticCustomItemHashMap.put(itemStack, dropWeight);

        } catch (NumberFormatException e) {

            Bukkit.getLogger().info("[EliteMobs] Your item " + itemStack.getItemMeta().getDisplayName() +
                    " contains an invalid drop weight value (" + dropType + ")");

        }

    }

    private static void rankedItemMapCreator(ItemStack itemStack) {

        int itemTier = (int) ItemTierFinder.findBattleTier(itemStack);

        if (CustomItemConstructor.dynamicRankedItemStacks.get(itemTier) == null) {

            List<ItemStack> list = new ArrayList<>();

            list.add(itemStack);

            CustomItemConstructor.dynamicRankedItemStacks.put(itemTier, list);

        } else {

            List<ItemStack> list = CustomItemConstructor.dynamicRankedItemStacks.get(itemTier);

            list.add(itemStack);

            CustomItemConstructor.dynamicRankedItemStacks.put(itemTier, list);

        }

    }

}
