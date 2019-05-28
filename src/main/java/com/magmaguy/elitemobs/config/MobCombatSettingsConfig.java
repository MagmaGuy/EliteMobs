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
    public static final String ELITEMOB_STACKING_CAP = "Elite mob stacking cap";
    public static final String PER_TIER_LEVEL_INCREASE = "Levels between elite mob tiers";
    public static final String STACK_AGGRESSIVE_SPAWNER_MOBS = "Stack aggressive spawner mobs";
    public static final String STACK_AGGRESSIVE_NATURAL_MOBS = "Stack aggressive natural mobs";
    public static final String AGGRESSIVE_STACK_RANGE = "Range to stack aggressive mobs";
    public static final String PASSIVE_STACK_RANGE = "Range to stack passive mobs";
    public static final String NATURAL_ELITEMOB_LEVEL_CAP = "Natural elite mob level cap";
    public final static String TARGET_HITS_TO_KILL = "Target hits to kill Elite Mobs";
    public final static String BASE_DAMAGE_DEALT_TO_PLAYER = "Base damage dealt to players from Elite Mobs";
    public static final String ELITE_CREEPER_EXPLOSION_MULTIPLIER = "SuperCreeper explosion nerf multiplier";
    public static final String ELITE_ARMOR = "Elite Mobs wear armor";
    public static final String ELITE_HELMETS = "Elite Mobs wear helmets";
    public static final String ENABLE_VISUAL_EFFECTS_FOR_NATURAL_MOBS = "Turn on visual effects for natural or plugin-spawned EliteMobs";
    public static final String DISABLE_VISUAL_EFFECTS_FOR_SPAWNER_MOBS = "Dangerous! Turn off visual effects for non-natural or non-plugin-spawned EliteMobs";
    public static final String ENABLE_WARNING_VISUAL_EFFECTS = "Turn on visual effects that indicate an attack is about to happen";
    public static final String ENABLE_DEATH_MESSAGES = "Enable EliteMobs death messages";
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
    public static final String HUSK_DEATH_MESSAGE = DEATH_MESSAGES + "husk";
    public static final String ZOMBIE_PIGMAN_DEATH_MESSAGE = DEATH_MESSAGES + "zombie pigman";
    public static final String STRAY_DEATH_MESSAGE = DEATH_MESSAGES + "stray";
    public static final String WITHER_SKELETON_DEATH_MESSAGE = DEATH_MESSAGES + "wither skeleton";
    public static final String DEFAULT_DEATH_MESSAGE = DEATH_MESSAGES + "default";
    public static final String DISPLAY_HEALTH_ON_HIT = "Display Elite Mob health on hit";
    public static final String DISPLAY_DAMAGE_ON_HIT = "Display damage dealt to Elite Mobs on hit";
    public static final String ONLY_SHOW_HEALTH_FOR_ELITE_MOBS = "Only display health indicator for Elite Mobs";
    public static final String ONLY_SHOW_DAMAGE_FOR_ELITE_MOBS = "Only display damage indicator for Elite Mobs";
    public static final String INCREASE_DIFFICULTY_WITH_SPAWN_DISTANCE = "Increase level of mobs spawned based on distance from world spawn";
    public static final String DISTANCE_TO_INCREMENT = "Distance between increments";
    public static final String LEVEL_TO_INCREMENT = "Amount of levels incremented per distance";
    public static final String OBFUSCATE_MOB_POWERS = "Hide mob powers until they are engaged";

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    public Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        configuration.addDefault(NATURAL_MOB_SPAWNING, true);
        configuration.addDefault(SPAWNERS_SPAWN_ELITE_MOBS, false);
        configuration.addDefault(AGGRESSIVE_MOB_CONVERSION_PERCENTAGE, 020.00);
        configuration.addDefault(AGGRESSIVE_MOB_STACKING, true);
        configuration.addDefault(ELITEMOB_STACKING_CAP, 100);
        configuration.addDefault(STACK_AGGRESSIVE_SPAWNER_MOBS, true);
        configuration.addDefault(STACK_AGGRESSIVE_NATURAL_MOBS, false);
        configuration.addDefault(AGGRESSIVE_STACK_RANGE, 1);
        configuration.addDefault(PASSIVE_STACK_RANGE, 15);
        configuration.addDefault(NATURAL_ELITEMOB_LEVEL_CAP, 3000);
        configuration.addDefault(TARGET_HITS_TO_KILL, 10);
        configuration.addDefault(BASE_DAMAGE_DEALT_TO_PLAYER, 2);
        configuration.addDefault(ELITE_CREEPER_EXPLOSION_MULTIPLIER, 1.0);
        configuration.addDefault(ELITE_ARMOR, true);
        configuration.addDefault(ELITE_HELMETS, true);
        configuration.addDefault(ENABLE_VISUAL_EFFECTS_FOR_NATURAL_MOBS, true);
        configuration.addDefault(DISABLE_VISUAL_EFFECTS_FOR_SPAWNER_MOBS, true);
        configuration.addDefault(ENABLE_WARNING_VISUAL_EFFECTS, true);
        configuration.addDefault(PER_TIER_LEVEL_INCREASE, 10);
        configuration.addDefault(ENABLE_DEATH_MESSAGES, true);
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
        configuration.addDefault(HUSK_DEATH_MESSAGE, "$player was hollowed out by $entity!");
        configuration.addDefault(ZOMBIE_PIGMAN_DEATH_MESSAGE, "$player was mobbed to death by $entity!");
        configuration.addDefault(STRAY_DEATH_MESSAGE, "$player was led astray by $entity!");
        configuration.addDefault(WITHER_SKELETON_DEATH_MESSAGE, "$entity's arrows withered away $player!");
        configuration.addDefault(DEFAULT_DEATH_MESSAGE, "$player has been slain by $entity");
        configuration.addDefault(DISPLAY_HEALTH_ON_HIT, true);
        configuration.addDefault(DISPLAY_DAMAGE_ON_HIT, true);
        configuration.addDefault(ONLY_SHOW_HEALTH_FOR_ELITE_MOBS, true);
        configuration.addDefault(ONLY_SHOW_DAMAGE_FOR_ELITE_MOBS, true);
        configuration.addDefault(INCREASE_DIFFICULTY_WITH_SPAWN_DISTANCE, false);
        configuration.addDefault(DISTANCE_TO_INCREMENT, 100);
        configuration.addDefault(LEVEL_TO_INCREMENT, 1);
        configuration.addDefault(OBFUSCATE_MOB_POWERS, true);

        configuration.options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
