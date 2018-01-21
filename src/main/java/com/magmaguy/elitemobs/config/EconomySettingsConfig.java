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

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class EconomySettingsConfig {

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();

    public void intializeEconomySettingsConfig() {

        this.getEconomySettingsConfig().addDefault("Enable economy", true);
        this.getEconomySettingsConfig().addDefault("Item resale value (percentage)", 5);
        this.getEconomySettingsConfig().addDefault("Tier price progression", 20);
        this.getEconomySettingsConfig().addDefault("Procedurally Generated Loot.Lowest simulated elite mob level loot", 1);
        this.getEconomySettingsConfig().addDefault("Procedurally Generated Loot.Highest simulated elite mob level loot", 100);
        this.getEconomySettingsConfig().addDefault("Custom Loot.Lowest simulated elite mob level loot", 1);
        this.getEconomySettingsConfig().addDefault("Custom Loot.Highest simulated elite mob level loot", 100);
        this.getEconomySettingsConfig().addDefault("Currency name", "Elite Coins");

        getEconomySettingsConfig().options().copyDefaults(true);
        saveDefaultCustomConfig();
        saveCustomConfig();

    }

    public FileConfiguration getEconomySettingsConfig() {

        return customConfigLoader.getCustomConfig("economySettings.yml");

    }

    public void reloadCustomConfig() {

        customConfigLoader.reloadCustomConfig("economySettings.yml");

    }

    public void saveCustomConfig() {

        customConfigLoader.saveCustomDefaultConfig("economySettings.yml");

    }

    public void saveDefaultCustomConfig() {

        customConfigLoader.saveDefaultCustomConfig("economySettings.yml");

    }

}
