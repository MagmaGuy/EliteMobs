package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.ChatColorConverter;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ItemSettingsConfig {

    public static boolean doEliteMobsLoot;
    public static boolean doMmorpgColors;
    public static boolean preventCustomItemPlacement;
    public static List<String> loreStructure;
    public static String shopItemSource, mobItemSource, loreWorth, loreResale;
    public static double flatDropRate, tierIncreaseDropRate;
    public static double proceduralItemWeight, weighedItemWeight, fixedItemWeight, limitedItemWeight, scalableItemWeight;
    public static double defaultLootMultiplier, defaultExperienceMultiplier;
    public static int maxLevelForDefaultLootMultiplier;
    public static int maximumLootTier;
    public static boolean useEliteEnchantments;
    public static boolean doRareDropsEffect;
    public static String eliteEnchantLoreString;
    public static boolean useHoesAsWeapons;

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("ItemSettings.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        doEliteMobsLoot = ConfigurationEngine.setBoolean(fileConfiguration, "doEliteMobsLoot", true);
        doMmorpgColors = ConfigurationEngine.setBoolean(fileConfiguration, "doMMORPGColorsForItems", true);
        preventCustomItemPlacement = ConfigurationEngine.setBoolean(fileConfiguration, "preventCustomItemPlacement", true);
        loreStructure = ConfigurationEngine.setList(fileConfiguration, "itemLoreStructure", Arrays.asList(
                ChatColorConverter.convert("$enchantments"),
                ChatColorConverter.convert("$potionEffect"),
                ChatColorConverter.convert("&m----------------------"),
                ChatColorConverter.convert("$customLore"),
                ChatColorConverter.convert("$itemSource"),
                ChatColorConverter.convert("$loreResaleValue"),
                ChatColorConverter.convert("Elite Mobs drop"),
                ChatColorConverter.convert("&m----------------------"),
                ChatColorConverter.convert("Tier $tier ")));
        shopItemSource = ConfigurationEngine.setString(fileConfiguration, "shopSourceItemLore", "Purchased from a store");
        mobItemSource = ConfigurationEngine.setString(fileConfiguration, "mobSourceItemLore", "Looted from $mob");
        loreWorth = ConfigurationEngine.setString(fileConfiguration, "loreWorth", "Worth $worth $currencyName");
        loreResale = ConfigurationEngine.setString(fileConfiguration, "loreResaleValue", "Sells for $resale $currencyName");
        flatDropRate = ConfigurationEngine.setDouble(fileConfiguration, "flatDropRate", 0.25);
        tierIncreaseDropRate = ConfigurationEngine.setDouble(fileConfiguration, "tierIncreaseDropRate", 0.05);
        proceduralItemWeight = ConfigurationEngine.setDouble(fileConfiguration, "proceduralItemDropWeight", 90);
        weighedItemWeight = ConfigurationEngine.setDouble(fileConfiguration, "weighedItemDropWeight", 1);
        fixedItemWeight = ConfigurationEngine.setDouble(fileConfiguration, "fixedItemDropWeight", 10);
        limitedItemWeight = ConfigurationEngine.setDouble(fileConfiguration, "limitedItemDropWeight", 3);
        scalableItemWeight = ConfigurationEngine.setDouble(fileConfiguration, "scalableItemDropWeight", 6);
        defaultLootMultiplier = ConfigurationEngine.setDouble(fileConfiguration, "defaultLootMultiplier", 0);
        maxLevelForDefaultLootMultiplier = ConfigurationEngine.setInt(fileConfiguration, "levelCapForDefaultLootMultiplier", 200);
        defaultExperienceMultiplier = ConfigurationEngine.setDouble(fileConfiguration, "defaultExperienceMultiplier", 1);
        maximumLootTier = ConfigurationEngine.setInt(fileConfiguration, "maximumLootTier", 100);
        useEliteEnchantments = ConfigurationEngine.setBoolean(fileConfiguration, "useEliteEnchantments", true);
        doRareDropsEffect = ConfigurationEngine.setBoolean(fileConfiguration, "doRareDropsEffect", true);
        eliteEnchantLoreString = ChatColorConverter.convert(ConfigurationEngine.setString(fileConfiguration, "eliteEnchantmentLoreString", "&6Elite"));
        useHoesAsWeapons = ConfigurationEngine.setBoolean(fileConfiguration, "useHoesAsWeapons", false);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }


}
