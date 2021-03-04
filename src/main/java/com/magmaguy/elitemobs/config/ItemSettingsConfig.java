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
    public static boolean enableRareItemParticleEffects;
    public static String potionEffectOnHitTargetLore, potionEffectOnHitSelfLore, potionEffectContinuousLore;
    public static String eliteEnchantmentColor;
    public static String vanillaEnchantmentColor;
    public static String customEnchantmentColor;
    public static String potionEffectColor;
    public static String noSoulbindLore;
    public static boolean preventEliteItemEnchantment;

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("ItemSettings.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        doEliteMobsLoot = ConfigurationEngine.setBoolean(fileConfiguration, "doEliteMobsLoot", true);
        doMmorpgColors = ConfigurationEngine.setBoolean(fileConfiguration, "doMMORPGColorsForItems", true);
        preventCustomItemPlacement = ConfigurationEngine.setBoolean(fileConfiguration, "preventCustomItemPlacement", true);
        loreStructure = ConfigurationEngine.setList(fileConfiguration, "itemLoreStructures", Arrays.asList(
                ChatColorConverter.convert("&7&m&l---------&7<&lEquip Info&7>&m&l---------"),
                ChatColorConverter.convert("&7Item level: &f$itemLevel &7Prestige &6$prestigeLevel"),
                ChatColorConverter.convert("$soulbindInfo"),
                ChatColorConverter.convert("$itemSource"),
                ChatColorConverter.convert("$ifLore&7&m&l-----------&7< &f&lLore&7 >&m&l-----------"),
                ChatColorConverter.convert("$customLore"),
                ChatColorConverter.convert("$ifEnchantments&7&m&l--------&7<&9&lEnchantments&7>&m&l--------"),
                ChatColorConverter.convert("$enchantments"),
                ChatColorConverter.convert("$eliteEnchantments"),
                ChatColorConverter.convert("$ifCustomEnchantments&7&m&l------&7< &3&lCustom Enchants&7 >&m&l------"),
                ChatColorConverter.convert("$customEnchantments"),
                ChatColorConverter.convert("$ifPotionEffects&7&m&l----------&7< &5&lEffects&7 >&m&l----------"),
                ChatColorConverter.convert("$potionEffect"),
                ChatColorConverter.convert("&7&l&m-----------------------------"),
                ChatColorConverter.convert("$loreResaleValue")
        ));
        shopItemSource = ConfigurationEngine.setString(fileConfiguration, "shopSourceItemLores", "&7Purchased from a store");
        mobItemSource = ConfigurationEngine.setString(fileConfiguration, "mobSourceItemLores", "&7Looted from $mob");
        loreWorth = ConfigurationEngine.setString(fileConfiguration, "loreWorths", "&7Worth $worth $currencyName");
        loreResale = ConfigurationEngine.setString(fileConfiguration, "loreResaleValues", "&7Sells for $resale $currencyName");
        flatDropRate = ConfigurationEngine.setDouble(fileConfiguration, "flatDropRateV2", 0.33);
        tierIncreaseDropRate = ConfigurationEngine.setDouble(fileConfiguration, "tierIncreaseDropRateV2", 0.00);
        proceduralItemWeight = ConfigurationEngine.setDouble(fileConfiguration, "proceduralItemDropWeight", 90);
        weighedItemWeight = ConfigurationEngine.setDouble(fileConfiguration, "weighedItemDropWeight", 1);
        fixedItemWeight = ConfigurationEngine.setDouble(fileConfiguration, "fixedItemDropWeight", 10);
        limitedItemWeight = ConfigurationEngine.setDouble(fileConfiguration, "limitedItemDropWeight", 3);
        scalableItemWeight = ConfigurationEngine.setDouble(fileConfiguration, "scalableItemDropWeight", 6);
        defaultLootMultiplier = ConfigurationEngine.setDouble(fileConfiguration, "defaultLootMultiplier", 0);
        maxLevelForDefaultLootMultiplier = ConfigurationEngine.setInt(fileConfiguration, "levelCapForDefaultLootMultiplier", 200);
        defaultExperienceMultiplier = ConfigurationEngine.setDouble(fileConfiguration, "defaultExperienceMultiplier", 1);
        maximumLootTier = ConfigurationEngine.setInt(fileConfiguration, "maximumLootTiers", 200);
        useEliteEnchantments = ConfigurationEngine.setBoolean(fileConfiguration, "useEliteEnchantments", true);
        doRareDropsEffect = ConfigurationEngine.setBoolean(fileConfiguration, "doRareDropsEffect", true);
        eliteEnchantLoreString = ChatColorConverter.convert(ConfigurationEngine.setString(fileConfiguration, "eliteEnchantmentLoreStrings", "Elite"));
        useHoesAsWeapons = ConfigurationEngine.setBoolean(fileConfiguration, "useHoesAsWeapons", false);
        enableRareItemParticleEffects = ConfigurationEngine.setBoolean(fileConfiguration, "enableRareItemParticleEffects", true);
        potionEffectOnHitTargetLore = ConfigurationEngine.setString(fileConfiguration, "potionEffectOnHitTargetLore", "&4⚔☠");
        potionEffectOnHitSelfLore = ConfigurationEngine.setString(fileConfiguration, "potionEffectOnHitSelfLore", "&9⚔🛡");
        potionEffectContinuousLore = ConfigurationEngine.setString(fileConfiguration, "potionEffectContinuousLore", "&6⟲");
        eliteEnchantmentColor = ConfigurationEngine.setString(fileConfiguration, "eliteEnchantmentLoreColor", "&9◇");
        vanillaEnchantmentColor = ConfigurationEngine.setString(fileConfiguration, "vanillaEnchantmentLoreColor", "&7◇");
        customEnchantmentColor = ConfigurationEngine.setString(fileConfiguration, "customEnchantmentColor", "&3◇");
        potionEffectColor = ConfigurationEngine.setString(fileConfiguration, "potionEffectLoreColor", "&5◇");
        noSoulbindLore = ConfigurationEngine.setString(fileConfiguration, "noSoulbindLore", "&7Not Soulbound!");
        preventEliteItemEnchantment = ConfigurationEngine.setBoolean(fileConfiguration, "preventEliteItemEnchantment", true);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }


}
