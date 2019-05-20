package com.magmaguy.elitemobs.items.customitems;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfig;
import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class CustomItem {

    public static HashMap<String, ItemStack> staticItem = new HashMap<>();

    public static ItemStack getStaticItem(String fileName) {
        return staticItem.get(fileName);
    }

    public static HashMap<String, CustomItem> dynamicItem = new HashMap<>();

    public static CustomItem getDynamicItem(String fileName) {
        return dynamicItem.get(fileName);
    }

    public static CustomItem getCustomItem(String fileName) {
        return dynamicItem.get(fileName);
    }

    public static void addCustomItem(String fileName, CustomLootConfig customLootConfig) {
        CustomItem.dynamicItem = dynamicItem;
    }

    private CustomLootConfigFields customLootConfigFields;

}
