package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.ChatColorConverter;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ItemSettingsConfig {
    @Getter
    private static String shopItemSource;
    @Getter
    private static boolean doEliteMobsLoot;
    @Getter
    private static boolean doMmorpgColors;
    @Getter
    private static boolean preventCustomItemPlacement;
    @Getter
    private static List<String> loreStructure;
    @Getter
    private static String mobItemSource;
    @Getter
    private static String loreWorth;
    @Getter
    private static String loreResale;
    @Getter
    private static double flatDropRate;
    @Getter
    private static double regionalBossNonUniqueDropRate;
    @Getter
    private static boolean regionalBossesDropVanillaLoot;
    @Getter
    private static double tierIncreaseDropRate;
    @Getter
    private static double proceduralItemWeight;
    @Getter
    private static double weighedItemWeight;
    @Getter
    private static double fixedItemWeight;
    @Getter
    private static double limitedItemWeight;
    @Getter
    private static double scalableItemWeight;
    @Getter
    private static double defaultLootMultiplier;
    @Getter
    private static double defaultExperienceMultiplier;
    @Getter
    private static String potionEffectOnHitTargetLore;
    @Getter
    private static int maxLevelForDefaultLootMultiplier;
    @Getter
    private static int maximumLootTier;
    @Getter
    private static boolean useEliteEnchantments;
    @Getter
    private static String eliteEnchantLoreString;
    @Getter
    private static boolean useHoesAsWeapons;
    @Getter
    private static boolean enableRareItemParticleEffects;
    @Getter
    private static String potionEffectOnHitSelfLore;
    @Getter
    private static String potionEffectContinuousLore;
    @Getter
    private static String scrapSucceededMessage;
    @Getter
    private static String eliteEnchantmentColor;
    @Getter
    private static String vanillaEnchantmentColor;
    @Getter
    private static String customEnchantmentColor;
    @Getter
    private static String potionEffectColor;
    @Getter
    private static String noSoulbindLore;
    @Getter
    private static boolean preventEliteItemEnchantment;
    @Getter
    private static boolean preventEliteItemDiamondToNetheriteUpgrade;
    @Getter
    private static boolean eliteDurability;
    @Getter
    private static double eliteDurabilityMultiplier;
    @Getter
    private static String scrapFailedMessage;
    @Getter
    private static boolean putLootDirectlyIntoPlayerInventory;
    @Getter
    private static int lootLevelDifferenceLockout;
    @Getter
    private static boolean preventEliteItemsFromBreaking;
    @Getter
    private static String lowArmorDurabilityItemDropMessage;
    @Getter
    private static String lowWeaponDurabilityItemDropMessage;
    @Getter
    private static int minimumProcedurallyGeneratedDiamondLootLevelPlusSeven;
    @Getter
    private static String simlootMessageSuccess;
    @Getter
    private static String simlootMessageFailure;
    @Getter
    private static String directDropCustomLootMessage;
    @Getter
    private static String directDropMinecraftLootMessage;
    @Getter
    private static String directDropCoinMessage;
    @Getter
    private static String directDropSpecialMessage;
    @Getter
    private static boolean hideAttributes;
    @Getter
    private static String weaponEntry;
    @Getter
    private static String armorEntry;

    private ItemSettingsConfig() {
    }

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("ItemSettings.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        doEliteMobsLoot = ConfigurationEngine.setBoolean(fileConfiguration, "doEliteMobsLoot", true);
        doMmorpgColors = ConfigurationEngine.setBoolean(fileConfiguration, "doMMORPGColorsForItems", true);
        preventCustomItemPlacement = ConfigurationEngine.setBoolean(fileConfiguration, "preventCustomItemPlacement", true);
        loreStructure = ConfigurationEngine.setList(file, fileConfiguration, "itemLoreStructureV2", Arrays.asList(
                ChatColorConverter.convert("&7&m&l---------&7<&lEquip Info&7>&m&l---------"),
                ChatColorConverter.convert("&7Item level: &f$itemLevel &7Prestige &6$prestigeLevel"),
                ChatColorConverter.convert("$weaponOrArmorStats"),
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
        ), true);
        shopItemSource = ConfigurationEngine.setString(file, fileConfiguration, "shopSourceItemLores", "&7Purchased from a store", true);
        mobItemSource = ConfigurationEngine.setString(file, fileConfiguration, "mobSourceItemLores", "&7Looted from $mob", true);
        loreWorth = ConfigurationEngine.setString(file, fileConfiguration, "loreWorths", "&7Worth $worth $currencyName", true);
        loreResale = ConfigurationEngine.setString(file, fileConfiguration, "loreResaleValues", "&7Sells for $resale $currencyName", true);
        flatDropRate = ConfigurationEngine.setDouble(fileConfiguration, "flatDropRateV3", 0.20);
        regionalBossNonUniqueDropRate = ConfigurationEngine.setDouble(fileConfiguration, "regionalBossNonUniqueDropRate", 0.05);
        regionalBossesDropVanillaLoot = ConfigurationEngine.setBoolean(fileConfiguration, "regionalBossesDropVanillaLoot", false);
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
        eliteEnchantLoreString = ChatColorConverter.convert(ConfigurationEngine.setString(file, fileConfiguration, "eliteEnchantmentLoreStrings", "Elite", true));
        useHoesAsWeapons = ConfigurationEngine.setBoolean(fileConfiguration, "useHoesAsWeapons", false);
        enableRareItemParticleEffects = ConfigurationEngine.setBoolean(fileConfiguration, "enableRareItemParticleEffects", true);
        potionEffectOnHitTargetLore = ConfigurationEngine.setString(file, fileConfiguration, "potionEffectOnHitTargetLore", "&4⚔☠", false);
        potionEffectOnHitSelfLore = ConfigurationEngine.setString(file, fileConfiguration, "potionEffectOnHitSelfLore", "&9⚔🛡", false);
        potionEffectContinuousLore = ConfigurationEngine.setString(file, fileConfiguration, "potionEffectContinuousLore", "&6⟲", false);
        eliteEnchantmentColor = ConfigurationEngine.setString(file, fileConfiguration, "eliteEnchantmentLoreColor", "&9◇", false);
        vanillaEnchantmentColor = ConfigurationEngine.setString(file, fileConfiguration, "vanillaEnchantmentLoreColor", "&7◇", false);
        customEnchantmentColor = ConfigurationEngine.setString(file, fileConfiguration, "customEnchantmentColor", "&3◇", false);
        potionEffectColor = ConfigurationEngine.setString(file, fileConfiguration, "potionEffectLoreColor", "&5◇", false);
        noSoulbindLore = ConfigurationEngine.setString(file, fileConfiguration, "noSoulbindLore", "&7Not Soulbound!", true);
        preventEliteItemEnchantment = ConfigurationEngine.setBoolean(fileConfiguration, "preventEliteItemEnchantment", true);
        preventEliteItemDiamondToNetheriteUpgrade = ConfigurationEngine.setBoolean(fileConfiguration, "preventEliteItemDiamondToNetheriteUpgrade", true);
        eliteDurability = ConfigurationEngine.setBoolean(fileConfiguration, "eliteItemsDurabilityLossOnlyOnDeath", true);
        eliteDurabilityMultiplier = ConfigurationEngine.setDouble(fileConfiguration, "eliteItemsDurabilityLossMultiplier", 1d);
        scrapSucceededMessage = ConfigurationEngine.setString(file, fileConfiguration, "scrapSucceededMessageV2", "&8[EliteMobs] &2Scrapping succeeded $amount times!", true);
        scrapFailedMessage = ConfigurationEngine.setString(file, fileConfiguration, "scrapFailedMessageV2", "&8[EliteMobs] &cScrapping failed $amount times!", true);
        putLootDirectlyIntoPlayerInventory = ConfigurationEngine.setBoolean(fileConfiguration, "putLootDirectlyIntoPlayerInventory", false);
        lootLevelDifferenceLockout = ConfigurationEngine.setInt(fileConfiguration, "lootLevelDifferenceLockout", 10);
        preventEliteItemsFromBreaking = ConfigurationEngine.setBoolean(fileConfiguration, "preventEliteItemsFromBreaking", true);
        lowArmorDurabilityItemDropMessage = ConfigurationEngine.setString(file, fileConfiguration, "lowDurabilityItemDropMessage", "&8[EliteMobs] &cDropped armor due to low durability! &8Repair it at the NPC with scrap to use it!", true);
        lowWeaponDurabilityItemDropMessage = ConfigurationEngine.setString(file, fileConfiguration, "lowWeaponItemDropMessage", "&8[EliteMobs] &cDropped weapon due to low durability! &8Repair it at the NPC with scrap to use it!", true);
        minimumProcedurallyGeneratedDiamondLootLevelPlusSeven = ConfigurationEngine.setInt(fileConfiguration, "minimumProcedurallyGeneratedDiamondLootLevelPlusSeven", 10);
        simlootMessageSuccess = ConfigurationEngine.setString(file, fileConfiguration, "simlootMessageSuccess", "&8[EliteMobs] &2Rolled for loot and got $itemName &2!", true);
        simlootMessageFailure = ConfigurationEngine.setString(file, fileConfiguration, "simlootMessageFailure", "&8[EliteMobs] &cRolled for loot and got nothing!", true);
        directDropCustomLootMessage = ConfigurationEngine.setString(file, fileConfiguration, "directDropCustomLootMessage", "&8[EliteMobs] &2Obtained $itemName &2!", true);
        directDropMinecraftLootMessage = ConfigurationEngine.setString(file, fileConfiguration, "directDropMinecraftLootMessage", "&8[EliteMobs] &aObtained $itemName &a!", true);
        directDropCoinMessage = ConfigurationEngine.setString(file, fileConfiguration, "directDropCoinMessage", "&8[EliteMobs] &aObtained &2$amount $currencyName &a!", true);
        directDropSpecialMessage = ConfigurationEngine.setString(file, fileConfiguration, "directDropSpecialMessage", "&8[EliteMobs] &aObtained &2$amount $name &a!", true);
        hideAttributes = ConfigurationEngine.setBoolean(fileConfiguration, "hideItemAttributes", true);
        weaponEntry = ConfigurationEngine.setString(file, fileConfiguration, "weaponEntry", "&7Elite DPS: &2$EDPS", true);
        armorEntry = ConfigurationEngine.setString(file, fileConfiguration, "armorEntry", "&7Elite Armor: &2$EDEF", true);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }
}
