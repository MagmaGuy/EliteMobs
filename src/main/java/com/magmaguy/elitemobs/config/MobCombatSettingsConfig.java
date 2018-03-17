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

public class MobCombatSettingsConfig {

    public static final String CONFIG_NAME = "MobCombatSettings.yml";

    public static final String NATURAL_MOB_SPAWNING = "Natural EliteMob spawning";
    public static final String PLAYER_DEATH_MESSAGE = "Player death message from Elite Mob";
    public static final String SPAWNERS_SPAWN_ELITE_MOBS = "Spawners can spawn Elite Mobs";
    public static final String AGGRESSIVE_MOB_CONVERSION_PERCENTAGE = "Percentage of aggressive mobs that get converted to EliteMobs when they spawn";
    public static final String AGGRESSIVE_MOB_STACKING = "Aggressive mob stacking";
    public static final String ELITEMOB_STACKING_CAP = "EliteMob stacking cap";
    public static final String STACK_AGGRESSIVE_SPAWNER_MOBS = "Stack aggressive spawner mobs";
    public static final String STACK_AGGRESSIVE_NATURAL_MOBS = "Stack aggressive natural mobs";
    public static final String NATURAL_ELITEMOB_LEVEL_CAP = "Natural elite mob level cap";
    public static final String LIFE_MULTIPLIER = "EliteMob life multiplier";
    public static final String DAMAGE_MULTIPLIER = "EliteMob damage multiplier";
    public static final String ELITE_CREEPER_EXPLOSION_MULTIPLIER = "SuperCreeper explosion nerf multiplier";
    public static final String ELITE_ARMOR = "Elite Mobs wear armor";
    public static final String ELITE_HELMETS = "Elite Mobs wear helmets";
    public static final String ENABLE_VISUAL_EFFECTS_FOR_NATURAL_MOBS = "Turn on visual effects for natural or plugin-spawned EliteMobs";
    public static final String DISABLE_VISUAL_EFFECTS_FOR_SPAWNER_MOBS = "Dangerous! Turn off visual effects for non-natural or non-plugin-spawned EliteMobs";
    public static final String ENABLE_WARNING_VISUAL_EFFECTS = "Turn on visual effects that indicate an attack is about to happen";
    public static final String ENABLE_DAMAGE_DISPLAY = " Display damage when Elite Mob is hit";
    public static final String ENABLE_HEALTH_DISPLAY = "Display health when Elite Mob is hit";

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        configuration.addDefault(NATURAL_MOB_SPAWNING, true);
        configuration.addDefault(PLAYER_DEATH_MESSAGE, "$player has been slain by $entity");
        configuration.addDefault(SPAWNERS_SPAWN_ELITE_MOBS, false);
        configuration.addDefault(AGGRESSIVE_MOB_CONVERSION_PERCENTAGE, 020.00);
        configuration.addDefault(AGGRESSIVE_MOB_STACKING, true);
        configuration.addDefault(ELITEMOB_STACKING_CAP, 100);
        configuration.addDefault(STACK_AGGRESSIVE_SPAWNER_MOBS, true);
        configuration.addDefault(STACK_AGGRESSIVE_NATURAL_MOBS, true);
        configuration.addDefault(NATURAL_ELITEMOB_LEVEL_CAP, 2500);
        configuration.addDefault(LIFE_MULTIPLIER, 1.0);
        configuration.addDefault(DAMAGE_MULTIPLIER, 1.0);
        configuration.addDefault(ELITE_CREEPER_EXPLOSION_MULTIPLIER, 1.0);
        configuration.addDefault(ELITE_ARMOR, true);
        configuration.addDefault(ELITE_HELMETS, true);
        configuration.addDefault(ENABLE_VISUAL_EFFECTS_FOR_NATURAL_MOBS, true);
        configuration.addDefault(DISABLE_VISUAL_EFFECTS_FOR_SPAWNER_MOBS, true);
        configuration.addDefault(ENABLE_WARNING_VISUAL_EFFECTS, true);
        configuration.addDefault(ENABLE_DAMAGE_DISPLAY, true);
        configuration.addDefault(ENABLE_HEALTH_DISPLAY, true);

        configuration.options().copyDefaults(true);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
