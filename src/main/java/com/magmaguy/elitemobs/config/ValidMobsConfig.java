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
    public static final String VALID_SUPERMOBS = "Valid Super Mobs.";
    public static final String CHICKEN = VALID_SUPERMOBS + "CHICKEN";
    public static final String COW = VALID_SUPERMOBS + "COW";
    public static final String MUSHROOM_COW = VALID_SUPERMOBS + "MUSHROOM_COW";
    public static final String PIG = VALID_SUPERMOBS + "PIG";
    public static final String SHEEP = VALID_SUPERMOBS + "SHEEP";
    public static final String VALID_AGGRESSIVE_ELITEMOBS = "Valid aggressive EliteMobs.";
    public static final String BLAZE = VALID_AGGRESSIVE_ELITEMOBS + "BLAZE";
    public static final String CAVE_SPIDER = VALID_AGGRESSIVE_ELITEMOBS + "CAVE_SPIDER";
    public static final String CREEPER = VALID_AGGRESSIVE_ELITEMOBS + "CREEPER";
    public static final String ENDERMAN = VALID_AGGRESSIVE_ELITEMOBS + "ENDERMAN";
    public static final String ENDERMITE = VALID_AGGRESSIVE_ELITEMOBS + "ENDERMITE";
    public static final String HUSK = VALID_AGGRESSIVE_ELITEMOBS + "HUSK";
    public static final String IRON_GOLEM = VALID_AGGRESSIVE_ELITEMOBS + "IRON_GOLEM";
    public static final String PIG_ZOMBIE = VALID_AGGRESSIVE_ELITEMOBS + "PIG_ZOMBIE";
    public static final String POLAR_BEAR = VALID_AGGRESSIVE_ELITEMOBS + "POLAR_BEAR";
    public static final String SILVERFISH = VALID_AGGRESSIVE_ELITEMOBS + "SILVERFISH";
    public static final String STRAY = VALID_AGGRESSIVE_ELITEMOBS + "STRAY";
    public static final String SKELETON = VALID_AGGRESSIVE_ELITEMOBS + "SKELETON";
    public static final String WITHER_SKELETON = VALID_AGGRESSIVE_ELITEMOBS + "WITHER_SKELETON";
    public static final String SPIDER = VALID_AGGRESSIVE_ELITEMOBS + "SPIDER";
    public static final String VEX = VALID_AGGRESSIVE_ELITEMOBS + "VEX";
    public static final String VINDICATOR = VALID_AGGRESSIVE_ELITEMOBS + "VINDICATOR";
    public static final String WITCH = VALID_AGGRESSIVE_ELITEMOBS + "WITCH";
    public static final String ZOMBIE = VALID_AGGRESSIVE_ELITEMOBS + "ZOMBIE";
    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    private Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        configuration.addDefault(ALLOW_AGGRESSIVE_ELITEMOBS, true);
        configuration.addDefault(BLAZE, true);
        configuration.addDefault(CAVE_SPIDER, true);
        configuration.addDefault(CREEPER, true);
        configuration.addDefault(ENDERMAN, true);
        configuration.addDefault(ENDERMITE, true);
        configuration.addDefault(HUSK, true);
        configuration.addDefault(IRON_GOLEM, true);
        configuration.addDefault(PIG_ZOMBIE, true);
        configuration.addDefault(POLAR_BEAR, true);
        configuration.addDefault(SILVERFISH, true);
        configuration.addDefault(SKELETON, true);
        configuration.addDefault(WITHER_SKELETON, true);
        configuration.addDefault(SPIDER, true);
        configuration.addDefault(STRAY, true);
        configuration.addDefault(VEX, true);
        configuration.addDefault(VINDICATOR, true);
        configuration.addDefault(WITCH, true);
        configuration.addDefault(ZOMBIE, true);
        configuration.addDefault(ALLOW_PASSIVE_SUPERMOBS, true);
        configuration.addDefault(CHICKEN, true);
        configuration.addDefault(COW, true);
        configuration.addDefault(MUSHROOM_COW, true);
        configuration.addDefault(PIG, true);
        configuration.addDefault(SHEEP, true);


        customConfigLoader.getCustomConfig(CONFIG_NAME).options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
