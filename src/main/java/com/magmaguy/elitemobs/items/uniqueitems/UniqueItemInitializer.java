package com.magmaguy.elitemobs.items.uniqueitems;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.ItemsUniqueConfig;
import org.bukkit.Bukkit;
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
            huntingHelmet.initializeItemStack();

            HuntingChestplate huntingChestplate = new HuntingChestplate();
            huntingChestplate.initializeItemStack();

            HuntingLeggings huntingLeggings = new HuntingLeggings();
            huntingLeggings.initializeItemStack();

            HuntingBoots huntingBoots = new HuntingBoots();
            huntingBoots.initializeItemStack();

            HuntingBow huntingBow = new HuntingBow();
            huntingBow.initializeItemStack();

        }

        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_ZOMBIE_KING_AXE)) {

            Bukkit.getLogger().info("ASDKJSAHDKSD");
            ZombieKingsAxe zombieKingsAxe = new ZombieKingsAxe();
            zombieKingsAxe.initializeItemStack();

        }

        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_KRAKEN_FISHING_ROD)) {

            DepthsSeeker depthsSeeker = new DepthsSeeker();
            depthsSeeker.initializeItemStack();

        }

        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_GREED)) {

            DwarvenGreed dwarvenGreed = new DwarvenGreed();
            dwarvenGreed.initializeItemStack();

        }

        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_THE_FELLER)) {

            TheFeller theFeller = new TheFeller();
            theFeller.initializeItemStack();

        }

    }

}
