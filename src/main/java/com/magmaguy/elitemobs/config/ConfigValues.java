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
            economyConfig, playerCacheConfig, eventsConfig, itemsCustomLootSettingsConfig, validMobsConfig, validWorldsConfig, itemsUniqueConfig;

    public static void initializeConfigValues() {

        CustomConfigLoader customConfigLoader = new CustomConfigLoader();

        defaultConfig = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getConfig();

        customConfigLoader = new CustomConfigLoader();
        itemsCustomLootListConfig = customConfigLoader.getCustomConfig(ItemsCustomLootListConfig.CONFIG_NAME);

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
        playerCacheConfig = customConfigLoader.getCustomConfig(PlayerCacheConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        eventsConfig = customConfigLoader.getCustomConfig(EventsConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        validMobsConfig = customConfigLoader.getCustomConfig(ValidMobsConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        validWorldsConfig = customConfigLoader.getCustomConfig(ValidWorldsConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        itemsUniqueConfig = customConfigLoader.getCustomConfig(ItemsUniqueConfig.CONFIG_NAME);

    }

}