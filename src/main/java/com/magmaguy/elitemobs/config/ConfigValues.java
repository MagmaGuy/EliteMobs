/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;

/**
 * Created by MagmaGuy on 05/05/2017.
 */
public class ConfigValues {

    public static Configuration defaultConfig, itemsCustomLootListConfig, mobPowerConfig, translationConfig, itemsProceduralSettingsConfig,
            economyConfig, playerCacheConfig, eventsConfig, itemsCustomLootSettingsConfig, validMobsConfig, validWorldsConfig, itemsUniqueConfig,
            mobCombatSettingsConfig, itemsDropSettingsConfig, playerMoneyData, playerRankData, playerMaxRankData, combatTagConfig, adventurersGuildConfig;

    public static void initializeConfigValues() {

        CustomConfigLoader customConfigLoader = new CustomConfigLoader();

        defaultConfig = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getConfig();

        customConfigLoader = new CustomConfigLoader();
        itemsCustomLootListConfig = customConfigLoader.getCustomConfig(ItemsCustomLootListConfig.CONFIG_NAME, true);

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
        itemsUniqueConfig = customConfigLoader.getCustomConfig(ItemsUniqueConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        mobCombatSettingsConfig = customConfigLoader.getCustomConfig(MobCombatSettingsConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        itemsDropSettingsConfig = customConfigLoader.getCustomConfig(ItemsDropSettingsConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        combatTagConfig = customConfigLoader.getCustomConfig(CombatTagConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        adventurersGuildConfig = customConfigLoader.getCustomConfig(AdventurersGuildConfig.CONFIG_NAME);

    }

    public static void intializeConfigurations() {

        DefaultConfig defaultConfig = new DefaultConfig();
        defaultConfig.loadConfiguration();

        MobPowersConfig mobPowersConfig = new MobPowersConfig();
        mobPowersConfig.initializeConfig();

        ItemsCustomLootListConfig itemsCustomLootListConfig = new ItemsCustomLootListConfig();
        itemsCustomLootListConfig.intializeConfig();

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

        ItemsUniqueConfig itemsUniqueConfig = new ItemsUniqueConfig();
        itemsUniqueConfig.initializeConfig();

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

    }

}