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
    private static final String DEATH_MESSAGES = "Player death messages cause by.";
    public static final String BLAZE_DEATH_MESSAGE = DEATH_MESSAGES + "blazes";
    public static final String CREEPER_DEATH_MESSAGE = DEATH_MESSAGES + "creeper";
    public static final String ENDERMAN_DEATH_MESSAGE = DEATH_MESSAGES + "enderman";
    public static final String ENDERMITE_DEATH_MESSAGE = DEATH_MESSAGES + "endermite";
    public static final String IRONGOLEM_DEATH_MESSAGE = DEATH_MESSAGES + "iron golem";
    public static final String POLARBEAR_DEATH_MESSAGE = DEATH_MESSAGES + "polar bear";
    public static final String SILVERFISH_DEATH_MESSAGE = DEATH_MESSAGES + "silverfish";
    public static final String SKELETON_DEATH_MESSAGE = DEATH_MESSAGES + "skeleton";
    public static final String SPIDER_DEATH_MESSAGE = DEATH_MESSAGES + "spider";
    public static final String WITCH_DEATH_MESSAGE = DEATH_MESSAGES + "witch";
    public static final String ZOMBIE_DEATH_MESSAGE = DEATH_MESSAGES + "zombie";
    public static final String DEFAULT_DEATH_MESSAGE = DEATH_MESSAGES + "default";
    public static final String DISPLAY_HEALTH_ON_HIT = "Display Elite Mob health on hit";
    public static final String DISPLAY_DAMAGE_ON_HIT = "Display damage deal to Elite Mobs on hit";
    public static final String ONLY_SHOW_HEALTH_FOR_ELITE_MOBS = "Only display health indicator for Elite Mobs";
    public static final String ONLY_SHOW_DAMAGE_FOR_ELITE_MOBS = "Only display damage indicator for Elite Mobs";

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        configuration.addDefault(NATURAL_MOB_SPAWNING, true);
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
        configuration.addDefault(BLAZE_DEATH_MESSAGE, "$player was lit ablaze by $entity!");
        configuration.addDefault(CREEPER_DEATH_MESSAGE, "$player was blasted away by $entity!");
        configuration.addDefault(ENDERMAN_DEATH_MESSAGE, "$entity sent $player into the void!");
        configuration.addDefault(ENDERMITE_DEATH_MESSAGE, "$player was glitched to death by $entity!");
        configuration.addDefault(IRONGOLEM_DEATH_MESSAGE, "$player messed with the wrong $entity!");
        configuration.addDefault(POLARBEAR_DEATH_MESSAGE, "$player was clawed to death by $entity!");
        configuration.addDefault(SILVERFISH_DEATH_MESSAGE, "$player mistook $entity for a stone block!");
        configuration.addDefault(SKELETON_DEATH_MESSAGE, "$player became $entity's pin cushion!");
        configuration.addDefault(SPIDER_DEATH_MESSAGE, "$player became entangled in $entity's web!");
        configuration.addDefault(WITCH_DEATH_MESSAGE, "$player became $entity's test subject!");
        configuration.addDefault(ZOMBIE_DEATH_MESSAGE, "$player was devoured by $entity!");
        configuration.addDefault(DEFAULT_DEATH_MESSAGE, "$player has been slain by $entity");
        configuration.addDefault(DISPLAY_HEALTH_ON_HIT, true);
        configuration.addDefault(DISPLAY_DAMAGE_ON_HIT, true);
        configuration.addDefault(ONLY_SHOW_HEALTH_FOR_ELITE_MOBS, true);
        configuration.addDefault(ONLY_SHOW_DAMAGE_FOR_ELITE_MOBS, true);

        configuration.options().copyDefaults(true);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
