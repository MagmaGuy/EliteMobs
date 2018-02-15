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

import org.bukkit.configuration.Configuration;

import java.util.Arrays;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class EconomySettingsConfig {

    public static final String CONFIG_NAME = "EconomySettings.yml";
    public static final String ENABLE_ECONOMY = "Enable economy";
    public static final String RESALE_VALUE = "Item resale value (percentage)";
    public static final String TIER_PRICE_PROGRESSION = "Tier price progression";
    public static final String LOWEST_PROCEDURALLY_SIMULATED_LOOT = "Procedurally Generated Loot.Lowest simulated elite mob level loot";
    public static final String HIGHEST_PROCEDURALLY_SIMULATED_LOOT = "Procedurally Generated Loot.Highest simulated elite mob level loot";
    public static final String LOWEST_SIMULATED_CUSTOM_LOOT = "Custom Loot.Lowest simulated elite mob level loot";
    public static final String HIGHEST_SIMULATED_CUSTOM_LOOT = "Custom Loot.Highest simulated elite mob level loot";
    public static final String CURRENCY_NAME = "Currency name";
    public static final String SHOP_NAME = "Shop name";
    public static final String CUSTOM_SHOP_NAME = "Custom shop name";
    public static final String SIGNATURE_ITEM_LOCATION_SHOPS = "Reroll button location for EliteMobs Shops";
    public static final String SHOP_VALID_SLOTS = "Valid chest slots for EliteMobs Shop";
    public static final String CUSTOM_SHOP_VALID_SLOTS = "Valid chest slots for EliteMobs Custom Shop";
    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        configuration.addDefault(ENABLE_ECONOMY, true);
        configuration.addDefault(RESALE_VALUE, 5);
        configuration.addDefault(TIER_PRICE_PROGRESSION, 20);
        configuration.addDefault(LOWEST_PROCEDURALLY_SIMULATED_LOOT, 1);
        configuration.addDefault(HIGHEST_PROCEDURALLY_SIMULATED_LOOT, 100);
        configuration.addDefault(LOWEST_SIMULATED_CUSTOM_LOOT, 1);
        configuration.addDefault(HIGHEST_SIMULATED_CUSTOM_LOOT, 100);
        configuration.addDefault(CURRENCY_NAME, "Elite Coins");
        configuration.addDefault(SHOP_NAME, "EliteMobs Shop");
        configuration.addDefault(CUSTOM_SHOP_NAME, "EliteMobs Custom Shop");
        configuration.addDefault(SIGNATURE_ITEM_LOCATION_SHOPS, 4);
        configuration.addDefault(SHOP_VALID_SLOTS, Arrays.asList(9, 10, 11, 12, 13, 14, 15, 16, 17, 18,
                19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45,
                46, 47, 48, 49, 50, 51, 52, 53));
        configuration.addDefault(CUSTOM_SHOP_VALID_SLOTS, Arrays.asList(9, 10, 11, 12, 13, 14, 15, 16, 17, 18,
                19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45,
                46, 47, 48, 49, 50, 51, 52, 53));

        configuration.options().copyDefaults(true);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
