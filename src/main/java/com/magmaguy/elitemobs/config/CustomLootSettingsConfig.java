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

public class CustomLootSettingsConfig {

    private CustomConfigLoader customConfigLoader = new CustomConfigLoader();

    public static final String SHOW_POTION_EFFECTS = "Show potion effects on lore";
    public static final String SHOW_ITEM_VALUE = "Show item value on lore";
    public static final String LORE_STRUCTURE = "Lore structure";
    public static final String DEFAULT_POTION_EFFECT_DURATION = "Default potion effect duration (seconds, on hit)";
    public static final String ABSORPTION = "ABSORPTION";
    public static final String BLINDNESS = "BLINDNESS";
    public static final String CONFUSION = "CONFUSION";
    public static final String DAMAGE_RESISTANCE = "DAMAGE_RESISTANCE";


    public void initializeCustomLootSettingsConfig() {

        this.getCustomLootSettingsConfig().addDefault(SHOW_POTION_EFFECTS, true);
        this.getCustomLootSettingsConfig().addDefault(SHOW_ITEM_VALUE, true);
        this.getCustomLootSettingsConfig().addDefault(LORE_STRUCTURE, "$potionEffect\n&m----------------------\n$customLore\n$itemValue\n&m----------------------");

        getCustomLootSettingsConfig().options().copyDefaults(true);
        saveDefaultCustomLootSettingsConfig();
        saveCustomLootSettingsConfig();

    }

    public FileConfiguration getCustomLootSettingsConfig() {

        return customConfigLoader.getCustomConfig("customLootSettings.yml");

    }

    public void reloadCustomLootSettingsConfig() {

        customConfigLoader.reloadCustomConfig("customLootSettings.yml");

    }

    public void saveCustomLootSettingsConfig() {

        customConfigLoader.saveCustomDefaultConfig("customLootSettings.yml");

    }

    public void saveDefaultCustomLootSettingsConfig() {

        customConfigLoader.saveDefaultCustomConfig("customLootSettings.yml");

    }


}
