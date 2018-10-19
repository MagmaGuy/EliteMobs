package com.magmaguy.elitemobs.items.uniqueitems;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.ItemsUniqueConfig;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class UniqueItemInitializer {

    /*
    Elements are added to the list during the construction process
     */
    public static List<ItemStack> uniqueItemsList = new ArrayList();

    public static void initialize() {

        if (!ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_UNIQUE_ITEMS)) return;

        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_HUNTING_SET)) {

            HuntingHelmet huntingHelmet = new HuntingHelmet();
            huntingHelmet.constructItemStack();

            HuntingChestplate huntingChestplate = new HuntingChestplate();
            huntingChestplate.constructItemStack();

            HuntingLeggings huntingLeggings = new HuntingLeggings();
            huntingLeggings.constructItemStack();

            HuntingBoots huntingBoots = new HuntingBoots();
            huntingBoots.constructItemStack();

            HuntingBow huntingBow = new HuntingBow();
            huntingBow.constructItemStack();

        }

        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_ZOMBIE_KING_AXE)) {

            ZombieKingsAxe zombieKingsAxe = new ZombieKingsAxe();
            zombieKingsAxe.constructItemStack();

        }

        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_KRAKEN_FISHING_ROD)) {

            DepthsSeeker depthsSeeker = new DepthsSeeker();
            depthsSeeker.constructItemStack();

        }

        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_GREED)) {

            DwarvenGreed dwarvenGreed = new DwarvenGreed();
            dwarvenGreed.constructItemStack();

        }

        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_THE_FELLER)) {

            TheFeller theFeller = new TheFeller();
            theFeller.constructItemStack();

        }

    }

}
