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

public class ValidMobsConfig {

    public static final String CONFIG_NAME = "ValidMobs.yml";
    public static final String ALLOW_AGGRESSIVE_ELITEMOBS = "Allow aggressive EliteMobs";
    public static final String ALLOW_PASSIVE_SUPERMOBS = "Allow Passive SuperMobs";
    public static final String CHICKEN = "Chicken";
    public static final String COW = "Cow";
    public static final String MUSHROOM_COW = "MushroomCow";
    public static final String PIG = "Pig";
    public static final String SHEEP = "Sheep";
    private static final String VALID_AGGRESSIVE_ELITEMOBS = "Valid aggressive EliteMobs.";
    public static final String BLAZE = VALID_AGGRESSIVE_ELITEMOBS + "Blaze";
    public static final String CAVE_SPIDER = VALID_AGGRESSIVE_ELITEMOBS + "CaveSpider";
    public static final String CREEPER = VALID_AGGRESSIVE_ELITEMOBS + "Creeper";
    public static final String ENDERMAN = VALID_AGGRESSIVE_ELITEMOBS + "Enderman";
    public static final String ENDERMITE = VALID_AGGRESSIVE_ELITEMOBS + "Endermite";
    public static final String IRON_GOLEM = VALID_AGGRESSIVE_ELITEMOBS + "IronGolem";
    public static final String PIG_ZOMBIE = VALID_AGGRESSIVE_ELITEMOBS + "PigZombie";
    public static final String POLAR_BEAR = VALID_AGGRESSIVE_ELITEMOBS + "PolarBear";
    public static final String SILVERFISH = VALID_AGGRESSIVE_ELITEMOBS + "Silverfish";
    public static final String SKELETON = VALID_AGGRESSIVE_ELITEMOBS + "Skeleton";
    public static final String SPIDER = VALID_AGGRESSIVE_ELITEMOBS + "Spider";
    public static final String WITCH = VALID_AGGRESSIVE_ELITEMOBS + "Witch";
    public static final String ZOMBIE = VALID_AGGRESSIVE_ELITEMOBS + "Zombie";
    private static final String VALID_PASSIVE_MOBS = "Valid passive EliteMobs.";
    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    private Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        configuration.addDefault(ALLOW_AGGRESSIVE_ELITEMOBS, true);
        configuration.addDefault(BLAZE, true);
        configuration.addDefault(CAVE_SPIDER, true);
        configuration.addDefault(CREEPER, true);
        configuration.addDefault(ENDERMAN, true);
        configuration.addDefault(ENDERMITE, true);
        configuration.addDefault(IRON_GOLEM, true);
        configuration.addDefault(PIG_ZOMBIE, true);
        configuration.addDefault(POLAR_BEAR, true);
        configuration.addDefault(SILVERFISH, true);
        configuration.addDefault(SKELETON, true);
        configuration.addDefault(SPIDER, true);
        configuration.addDefault(WITCH, true);
        configuration.addDefault(ZOMBIE, true);
        configuration.addDefault(ALLOW_PASSIVE_SUPERMOBS, true);
        configuration.addDefault("Valid passive EliteMobs.Chicken", true);
        configuration.addDefault("Valid passive EliteMobs.Cow", true);
        configuration.addDefault("Valid passive EliteMobs.MushroomCow", true);
        configuration.addDefault("Valid passive EliteMobs.Pig", true);
        configuration.addDefault("Valid passive EliteMobs.Sheep", true);


        customConfigLoader.getCustomConfig(CONFIG_NAME).options().copyDefaults(true);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
