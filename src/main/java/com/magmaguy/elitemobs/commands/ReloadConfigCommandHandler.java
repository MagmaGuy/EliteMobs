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

package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.LootCustomConfig;
import com.magmaguy.elitemobs.config.MobPowersCustomConfig;
import com.magmaguy.elitemobs.config.TranslationCustomConfig;
import com.magmaguy.elitemobs.elitedrops.EliteDropsHandler;
import org.bukkit.Bukkit;

/**
 * Created by MagmaGuy on 13/05/2017.
 */
public class ReloadConfigCommandHandler {

    public void reloadConfiguration() {

        //Reload all configs
        Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).reloadConfig();
        MobPowersCustomConfig mobPowersCustomConfig = new MobPowersCustomConfig();
        mobPowersCustomConfig.reloadCustomConfig();
        LootCustomConfig lootCustomConfig = new LootCustomConfig();
        lootCustomConfig.reloadLootConfig();
        TranslationCustomConfig translationCustomConfig = new TranslationCustomConfig();
        translationCustomConfig.reloadCustomConfig();
        ConfigValues configValues = new ConfigValues();
        configValues.initializeConfigValues();

        //reload config-based initialized data
        EliteDropsHandler eliteDropsHandler = new EliteDropsHandler();
        eliteDropsHandler.superDropParser();

        Bukkit.getLogger().info("EliteMobs config reloaded!");

    }

}
