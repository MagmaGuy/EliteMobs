package com.magmaguy.elitemobs.items.uniqueitems;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.ItemsUniqueConfig;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class UniqueItemInitializer {

    public static List<ItemStack> uniqueItemsList = new ArrayList();

    public static void initialize() {

        if (!ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_UNIQUE_ITEMS)) return;

        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_HUNTING_SET)) {

            HuntingHelmet huntingHelmet = new HuntingHelmet();
            ItemStack huntingHelmetItem = huntingHelmet.constructItemStack();
            uniqueItemsList.add(huntingHelmetItem);

            HuntingChestplate huntingChestplate = new HuntingChestplate();
            ItemStack huntingChestplateItem = huntingChestplate.constructItemStack();
            uniqueItemsList.add(huntingChestplateItem);

            HuntingLeggings huntingLeggings = new HuntingLeggings();
            ItemStack huntingLeggingsItem = huntingLeggings.constructItemStack();
            uniqueItemsList.add(huntingLeggingsItem);

            HuntingBoots huntingBoots = new HuntingBoots();
            ItemStack huntingBootsItem = huntingBoots.constructItemStack();
            uniqueItemsList.add(huntingBootsItem);

            HuntingBow huntingBow = new HuntingBow();
            ItemStack huntingBowItem = huntingBow.constructItemStack();
            uniqueItemsList.add(huntingBowItem);

        }

        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_ZOMBIE_KING_AXE)) {

            ZombieKingsAxe zombieKingsAxe = new ZombieKingsAxe();
            ItemStack zombieKingsAxeItem = zombieKingsAxe.constructItemStack();
            uniqueItemsList.add(zombieKingsAxeItem);

        }

        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_KRAKEN_FISHING_ROD)) {

            DepthsSeeker depthsSeeker = new DepthsSeeker();
            ItemStack depthsSeekerItem = depthsSeeker.constructItemStack();
            uniqueItemsList.add(depthsSeekerItem);

        }

        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_GREED)) {

            DwarvenGreed dwarvenGreed = new DwarvenGreed();
            ItemStack dwarvenGreedItem = dwarvenGreed.constructItemStack();
            uniqueItemsList.add(dwarvenGreedItem);

        }

        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_THE_FELLER)) {

            TheFeller theFeller = new TheFeller();
            ItemStack theFellerItem = theFeller.constructItemStack();
            uniqueItemsList.add(theFellerItem);

        }

    }

}
