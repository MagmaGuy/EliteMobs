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

    public static final String ENABLE_ECONOMY = "Enable economy";
    public static final String RESALE_VALUE = "Item resale value (percentage)";
    public static final String TIER_PRICE_PROGRESSION = "Tier price progression";
    public static final String LOWEST_PROCEDURALLY_SIMULATED_LOOT = "Procedurally Generated Loot.Lowest simulated elite mob level loot";
    public static final String HIGHEST_PROCEDURALLY_SIMULATED_LOOT = "Procedurally Generated Loot.Highest simulated elite mob level loot";
    public static final String LOWEST_SIMULATED_CUSTOM_LOOT = "Custom Loot.Lowest simulated elite mob level loot";
    public static final String HIGHEST_SIMULATED_CUSTOM_LOOT = "Custom Loot.Highest simulated elite mob level loot";
    public static final String CURRENCY_NAME = "Currency name";

    public void intializeEconomySettingsConfig() {

        this.getEconomySettingsConfig().addDefault(ENABLE_ECONOMY, true);
        this.getEconomySettingsConfig().addDefault(RESALE_VALUE, 5);
        this.getEconomySettingsConfig().addDefault(TIER_PRICE_PROGRESSION, 20);
        this.getEconomySettingsConfig().addDefault(LOWEST_PROCEDURALLY_SIMULATED_LOOT, 1);
        this.getEconomySettingsConfig().addDefault(HIGHEST_PROCEDURALLY_SIMULATED_LOOT, 100);
        this.getEconomySettingsConfig().addDefault(LOWEST_SIMULATED_CUSTOM_LOOT, 1);
        this.getEconomySettingsConfig().addDefault(HIGHEST_SIMULATED_CUSTOM_LOOT, 100);
        this.getEconomySettingsConfig().addDefault(CURRENCY_NAME, "Elite Coins");

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
