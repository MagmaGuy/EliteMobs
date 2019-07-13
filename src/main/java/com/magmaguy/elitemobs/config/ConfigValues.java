package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;

/**
 * Created by MagmaGuy on 05/05/2017.
 */
public class ConfigValues {

    public static Configuration defaultConfig,
            mobPowerConfig,
            translationConfig,
            itemsProceduralSettingsConfig,
            economyConfig,
            playerCacheConfig,
            eventsConfig,
            itemsCustomLootSettingsConfig,
            mobCombatSettingsConfig,
            itemsDropSettingsConfig,
            playerMaxRankData;

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
        playerMaxRankData = customConfigLoader.getCustomConfig(PlayerMaxGuildRank.CONFIG_NAME, true);

        customConfigLoader = new CustomConfigLoader();
        eventsConfig = customConfigLoader.getCustomConfig(EventsConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        mobCombatSettingsConfig = customConfigLoader.getCustomConfig(MobCombatSettingsConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        itemsDropSettingsConfig = customConfigLoader.getCustomConfig(ItemsDropSettingsConfig.CONFIG_NAME);
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

        PlayerCacheData playerCacheData = new PlayerCacheData();
        playerCacheData.initializeConfig();

        EventsConfig eventsConfig = new EventsConfig();
        eventsConfig.initializeConfig();

        MobCombatSettingsConfig mobCombatSettingsConfig = new MobCombatSettingsConfig();
        mobCombatSettingsConfig.initializeConfig();

        ItemsDropSettingsConfig itemsDropSettingsConfig = new ItemsDropSettingsConfig();
        itemsDropSettingsConfig.initializeConfig();

        PlayerMaxGuildRank playerMaxGuildRank = new PlayerMaxGuildRank();
        playerMaxGuildRank.intializeConfig();

    }

}