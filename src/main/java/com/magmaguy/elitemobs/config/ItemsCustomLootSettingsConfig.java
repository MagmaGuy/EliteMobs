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

public class ItemsCustomLootSettingsConfig {

    public static final String CONFIG_NAME = "ItemsCustomLootSettings.yml";
    public static final String SHOW_POTION_EFFECTS = "Show potion effects on lore";
    public static final String SHOW_ITEM_VALUE = "Show item value on lore";
    public static final String LORE_STRUCTURE = "Lore structure";
    public static final String PREVENT_CUSTOM_ITEM_PLACING = "Prevent players from placing pleaceable Elite custom items";
    public static final String DEFAULT_POTION_EFFECT_DURATION = "Default potion effect duration (seconds, on hit)";
    public static final String ABSORPTION = "ABSORPTION";
    public static final String BLINDNESS = "BLINDNESS";
    public static final String CONFUSION = "CONFUSION";
    public static final String DAMAGE_RESISTANCE = "DAMAGE_RESISTANCE";
    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    private Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        configuration.addDefault(SHOW_POTION_EFFECTS, true);
        configuration.addDefault(SHOW_ITEM_VALUE, true);
        configuration.addDefault(PREVENT_CUSTOM_ITEM_PLACING, true);
        configuration.addDefault(LORE_STRUCTURE, "$potionEffect\n&m----------------------\n$customLore\n$itemValue\n&m----------------------");

        customConfigLoader.getCustomConfig(CONFIG_NAME).options().copyDefaults(true);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
