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
    public static final String DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX = "Default potion effect duration (seconds, on hit).";
    public static final String ABSORPTION = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "ABSORPTION";
    public static final String BLINDNESS = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "BLINDNESS";
    public static final String CONFUSION = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "CONFUSION";
    public static final String DAMAGE_RESISTANCE = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "DAMAGE_RESISTANCE";
    public static final String FAST_DIGGING = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "FAST_DIGGING";
    public static final String FIRE_RESISTANCE = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "FIRE_RESISTANCE";
    public static final String GLOWING = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "GLOWING";
    public static final String HEALTH_BOOST = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "HEALTH_BOOST";
    public static final String HUNGER = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "HUNGER";
    public static final String INCREASE_DAMAGE = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "INCREASE_DAMAGE";
    public static final String INVISIBILITY = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "INVISIBILITY";
    public static final String JUMP = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "LEVITATION";
    public static final String LUCK = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "LUCK";
    public static final String NIGHT_VISION = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "NIGHT_VISION";
    public static final String POISON = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "POISON";
    public static final String REGENERATION = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "REGENERATION";
    public static final String SLOW = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "SLOW";
    public static final String SLOW_DIGGING = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "SLOW_DIGGING";
    public static final String SPEED = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "SPEED";
    public static final String UNLUCK = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "UNLUCK";
    public static final String WATER_BREATHING = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "WATER_BREATHING";
    public static final String WEAKNESS = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "WEAKNESS";
    public static final String WITHER = DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + "WITHER";
    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    private Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        configuration.addDefault(SHOW_POTION_EFFECTS, true);
        configuration.addDefault(SHOW_ITEM_VALUE, true);
        configuration.addDefault(PREVENT_CUSTOM_ITEM_PLACING, true);
        configuration.addDefault(LORE_STRUCTURE, "$potionEffect\n&m----------------------\n$customLore\n$itemValue\n&m----------------------");
        configuration.addDefault(ABSORPTION, 2);
        configuration.addDefault(BLINDNESS, 5);
        configuration.addDefault(CONFUSION, 10);
        configuration.addDefault(DAMAGE_RESISTANCE, 30);
        configuration.addDefault(FAST_DIGGING, 30);
        configuration.addDefault(FIRE_RESISTANCE, 15);
        configuration.addDefault(GLOWING, 60);
        configuration.addDefault(HEALTH_BOOST, 90);
        configuration.addDefault(HUNGER, 30);
        configuration.addDefault(INCREASE_DAMAGE, 30);
        configuration.addDefault(INVISIBILITY, 3);
        configuration.addDefault(JUMP, 10);
        configuration.addDefault(LUCK, 5);
        configuration.addDefault(NIGHT_VISION, 30);
        configuration.addDefault(POISON, 20);
        configuration.addDefault(REGENERATION, 20);
        configuration.addDefault(SLOW, 15);
        configuration.addDefault(SLOW_DIGGING, 10);
        configuration.addDefault(SPEED, 30);
        configuration.addDefault(UNLUCK, 30);
        configuration.addDefault(WATER_BREATHING, 30);
        configuration.addDefault(WEAKNESS, 15);
        configuration.addDefault(WITHER, 15);

        customConfigLoader.getCustomConfig(CONFIG_NAME).options().copyDefaults(true);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
