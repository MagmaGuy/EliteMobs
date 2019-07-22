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

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("AntiExploit.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        doEliteMobsLoot = ConfigurationEngine.setBoolean(fileConfiguration, "doEliteMobsLoot", true);
        doMmorpgColors = ConfigurationEngine.setBoolean(fileConfiguration, "doMMORPGColorsForItems", true);
        preventCustomItemPlacement = ConfigurationEngine.setBoolean(fileConfiguration, "preventCustomItemPlacement", true);
        loreStructure = ConfigurationEngine.setList(fileConfiguration, "itemLoreStructure", Arrays.asList(
                ChatColorConverter.convert("$enchantments"),
                ChatColorConverter.convert("$potionEffect"),
                ChatColorConverter.convert("&m----------------------"),
                ChatColorConverter.convert("$customLore")
                , ChatColorConverter.convert("$itemSource"),
                ChatColorConverter.convert("$loreResaleValue"),
                ChatColorConverter.convert("Elite Mobs drop"),
                ChatColorConverter.convert("&m----------------------"),
                ChatColorConverter.convert("Tier $tier ")));
        shopItemSource = ConfigurationEngine.setString(fileConfiguration, "shopSourceItemLore", "Purchased from a store");
        mobItemSource = ConfigurationEngine.setString(fileConfiguration, "mobSourceItemLore", "Looted from $mob");
        loreWorth = ConfigurationEngine.setString(fileConfiguration, "loreWorth", "Worth $worth $currencyName");
        loreResale = ConfigurationEngine.setString(fileConfiguration, "loreResaleValue", "Sells for $resale $currencyName");

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }


}
