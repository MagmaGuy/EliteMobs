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

   public static Configuration defaultConfig, lootConfig, mobPowerConfig, translationConfig, randomItemsConfig;

    public void initializeConfigValues () {

        defaultConfig = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getConfig();
        LootCustomConfig lootCustomConfig = new LootCustomConfig();
        lootConfig = lootCustomConfig.getLootConfig();
        MobPowersCustomConfig mobPowersCustomConfig = new MobPowersCustomConfig();
        mobPowerConfig = mobPowersCustomConfig.getMobPowersConfig();
        TranslationCustomConfig translationCustomConfig = new TranslationCustomConfig();
        translationConfig = translationCustomConfig.getTranslationConfig();
        RandomItemsSettingsConfig randomItemsSettingsConfig = new RandomItemsSettingsConfig();
        randomItemsConfig = randomItemsSettingsConfig.getRandomItemsSettingsConfig();

    }

}