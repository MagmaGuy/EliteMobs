package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;

/**
 * Created by MagmaGuy on 05/05/2017.
 */
public class ConfigValues {

    public static Configuration defaultConfig, mobPowerConfig, translationConfig,
            itemsProceduralSettingsConfig, economyConfig, playerCacheConfig, eventsConfig, itemsCustomLootSettingsConfig,
            validMobsConfig, validWorldsConfig, mobCombatSettingsConfig, itemsDropSettingsConfig,
            playerMoneyData, playerRankData, playerMaxRankData, combatTagConfig, adventurersGuildConfig,
            customEnchantmentsConfig, npcConfig;

    public static void initializeCachedConfigurations() {

        CustomConfigLoader customConfigLoader = new CustomConfigLoader();

        defaultConfig = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getConfig();

        customConfigLoader = new CustomConfigLoader();
        mobPowerConfig = customConfigLoader.getCustomConfig(MobPowersConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        itemsCustomLootSettingsConfig = customConfigLoader.getCustomConfig(ItemsCustomLootSettingsConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        translationConfig = customConfigLoader.getCustomConfig(TranslationConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        itemsProceduralSettingsConfig = customConfigLoader.getCustomConfig(ItemsProceduralSettingsConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        economyConfig = customConfigLoader.getCustomConfig(EconomySettingsConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        playerCacheConfig = customConfigLoader.getCustomConfig(PlayerCacheData.CONFIG_NAME, true);

        customConfigLoader = new CustomConfigLoader();
        playerMoneyData = customConfigLoader.getCustomConfig(PlayerMoneyData.CONFIG_NAME, true);

        customConfigLoader = new CustomConfigLoader();
        playerRankData = customConfigLoader.getCustomConfig(PlayerGuildRank.CONFIG_NAME, true);

        customConfigLoader = new CustomConfigLoader();
        playerMaxRankData = customConfigLoader.getCustomConfig(PlayerMaxGuildRank.CONFIG_NAME, true);

        customConfigLoader = new CustomConfigLoader();
        eventsConfig = customConfigLoader.getCustomConfig(EventsConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        validMobsConfig = customConfigLoader.getCustomConfig(ValidMobsConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        validWorldsConfig = customConfigLoader.getCustomConfig(ValidWorldsConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        mobCombatSettingsConfig = customConfigLoader.getCustomConfig(MobCombatSettingsConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        itemsDropSettingsConfig = customConfigLoader.getCustomConfig(ItemsDropSettingsConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        combatTagConfig = customConfigLoader.getCustomConfig(CombatTagConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        adventurersGuildConfig = customConfigLoader.getCustomConfig(AdventurersGuildConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        customEnchantmentsConfig = customConfigLoader.getCustomConfig(CustomEnchantmentsConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        npcConfig = customConfigLoader.getCustomConfig(NPCConfig.CONFIG_NAME);

    }

    public static void initializeConfigurations() {

        DefaultConfig defaultConfig = new DefaultConfig();
        defaultConfig.loadConfiguration();

        MobPowersConfig mobPowersConfig = new MobPowersConfig();
        mobPowersConfig.initializeConfig();

        ItemsCustomLootSettingsConfig itemsCustomLootSettingsConfig = new ItemsCustomLootSettingsConfig();
        itemsCustomLootSettingsConfig.initializeConfig();

        TranslationConfig translationConfig = new TranslationConfig();
        translationConfig.initializeConfig();

        ItemsProceduralSettingsConfig itemsProceduralSettingsConfig = new ItemsProceduralSettingsConfig();
        itemsProceduralSettingsConfig.intializeConfig();

        EconomySettingsConfig economySettingsConfig = new EconomySettingsConfig();
        economySettingsConfig.initializeConfig();

        PlayerMoneyData playerMoneyData = new PlayerMoneyData();
        playerMoneyData.intializeConfig();

        PlayerCacheData playerCacheData = new PlayerCacheData();
        playerCacheData.initializeConfig();

        EventsConfig eventsConfig = new EventsConfig();
        eventsConfig.initializeConfig();

        ValidWorldsConfig validWorldsConfig = new ValidWorldsConfig();
        validWorldsConfig.initializeConfig();

        ValidMobsConfig validMobsConfig = new ValidMobsConfig();
        validMobsConfig.initializeConfig();

        MobCombatSettingsConfig mobCombatSettingsConfig = new MobCombatSettingsConfig();
        mobCombatSettingsConfig.initializeConfig();

        ItemsDropSettingsConfig itemsDropSettingsConfig = new ItemsDropSettingsConfig();
        itemsDropSettingsConfig.initializeConfig();

        PlayerGuildRank playerGuildRank = new PlayerGuildRank();
        playerGuildRank.intializeConfig();

        PlayerMaxGuildRank playerMaxGuildRank = new PlayerMaxGuildRank();
        playerMaxGuildRank.intializeConfig();

        CombatTagConfig combatTagConfig = new CombatTagConfig();
        combatTagConfig.initializeConfig();

        AdventurersGuildConfig adventurersGuildConfig = new AdventurersGuildConfig();
        adventurersGuildConfig.initializeConfig();

        CustomEnchantmentsConfig customEnchantmentsConfig = new CustomEnchantmentsConfig();
        customEnchantmentsConfig.initializeConfig();

        NPCConfig npcConfig = new NPCConfig();
        npcConfig.initializeConfig();

    }

}