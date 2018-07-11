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

public class ItemsDropSettingsConfig {

    public static final String CONFIG_NAME = "ItemsDropSettings.yml";

    public static final String ENABLE_PLUGIN_LOOT = "Enable plugin loot";
    public static final String DROP_CUSTOM_ITEMS = "Elite Mobs can drop custom loot";
    public static final String ELITE_ITEM_FLAT_DROP_RATE = "EliteMob base percentual plugin item drop chance";
    public static final String ELITE_ITEM_LEVEL_DROP_RATE = "EliteMob plugin item percentual drop chance increase per level";
    public static final String PROCEDURAL_ITEM_WEIGHT = "Procedurally generated item weight";
    public static final String CUSTOM_DYNAMIC_ITEM_WEIGHT = "Custom dynamic item weight";
    public static final String CUSTOM_STATIC_ITEM_WEIGHT = "Custom static item weight";
    public static final String SPAWNER_DEFAULT_LOOT_MULTIPLIER = "Drop multiplied default loot from elite mobs spawned in spawners";
    public static final String DEFAULT_LOOT_MULTIPLIER = "EliteMob default loot multiplier";
    public static final String MAXIMUM_LEVEL_FOR_LOOT_MULTIPLIER = "Maximum level of the Elite Mob default multiplier";
    public static final String EXPERIENCE_LOOT_MULTIPLIER = "EliteMob xp multiplier";
    public static final String MAXIMUM_LOOT_TIER = "Maximum loot tier (REQUIRES INCREASING SHARPNESS AND PROTECTION ENCHANTMENTS TO WORK PROPERLY, READ GITHUB WIKI!)";

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        configuration.addDefault(ENABLE_PLUGIN_LOOT, true);
        configuration.addDefault(DROP_CUSTOM_ITEMS, true);
        configuration.addDefault(ELITE_ITEM_FLAT_DROP_RATE, 025.00);
        configuration.addDefault(ELITE_ITEM_LEVEL_DROP_RATE, 001.00);
        configuration.addDefault(PROCEDURAL_ITEM_WEIGHT, 90);
        configuration.addDefault(CUSTOM_DYNAMIC_ITEM_WEIGHT, 9);
        configuration.addDefault(CUSTOM_STATIC_ITEM_WEIGHT, 1);
        configuration.addDefault(SPAWNER_DEFAULT_LOOT_MULTIPLIER, true);
        configuration.addDefault(DEFAULT_LOOT_MULTIPLIER, 1.0);
        configuration.addDefault(MAXIMUM_LEVEL_FOR_LOOT_MULTIPLIER, 1000);
        configuration.addDefault(EXPERIENCE_LOOT_MULTIPLIER, 1.0);
        configuration.addDefault(MAXIMUM_LOOT_TIER, 5);

        configuration.options().copyDefaults(true);
        configuration = UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
