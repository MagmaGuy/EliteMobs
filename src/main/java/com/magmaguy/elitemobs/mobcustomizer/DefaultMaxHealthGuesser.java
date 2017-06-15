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

package com.magmaguy.elitemobs.mobcustomizer;

import org.bukkit.entity.Entity;

import static org.bukkit.Bukkit.getLogger;

/**
 * Created by MagmaGuy on 02/11/2016.
 */
public class DefaultMaxHealthGuesser {

    public static final double zombieHealth = 20;
    public static final double huskHealth = 20;
    public static final double zombieVillagerHealth = 20;
    public static final double skeletonHealth = 20;
    public static final double witherSkeletonHealth = 20;
    public static final double strayHealth = 20;
    public static final double pigZombieHealth = 20;
    public static final double creeperHealth = 20;
    public static final double spiderHealth = 16;
    public static final double endermanHealth = 40;
    public static final double caveSpiderHealth = 12;
    public static final double silverfishHealth = 8;
    public static final double blazeHealth = 20;
    public static final double witchHealth = 26;
    public static final double endermiteHealth = 8;
    public static final double polarBearHealth = 30;

    public static final double chickenHealth = 4;
    public static final double cowHealthHealth = 10;
    public static final double ironGolemHealth = 100;
    public static final double mushroomCowHealth = 10;
    public static final double pigHealth = 10;
    public static final double sheepHealth = 8;

    public static double defaultMaxHealthGuesser(Entity entity) {

        //TODO: replace this with a better method if one is found

        switch (entity.getType()) {
            case ZOMBIE:
                return zombieHealth;
            case HUSK:
                return huskHealth;
            case ZOMBIE_VILLAGER:
                return zombieVillagerHealth;
            case SKELETON:
                return skeletonHealth;
            case WITHER_SKELETON:
                return witherSkeletonHealth;
            case STRAY:
                return strayHealth;
            case PIG_ZOMBIE:
                return pigZombieHealth;
            case CREEPER:
                return creeperHealth;
            case SPIDER:
                return spiderHealth;
            case ENDERMAN:
                return endermanHealth;
            case CAVE_SPIDER:
                return caveSpiderHealth;
            case SILVERFISH:
                return silverfishHealth;
            case BLAZE:
                return blazeHealth;
            case WITCH:
                return witchHealth;
            case ENDERMITE:
                return endermiteHealth;
            case POLAR_BEAR:
                return polarBearHealth;
            case CHICKEN:
                return chickenHealth;
            case COW:
                return cowHealthHealth;
            case IRON_GOLEM:
                return ironGolemHealth;
            case MUSHROOM_COW:
                return mushroomCowHealth;
            case PIG:
                return pigHealth;
            case SHEEP:
                return sheepHealth;
            default:
                getLogger().info("Error: Couldn't assign custom mob name due to unexpected boss mob (talk to the dev!)");
                getLogger().info("Missing mob type: " + entity.getType());
                break;
        }

        //Should never return this.

        getLogger().info("Something has gone wrong with guessing the max health! Defaulting to 0. Talk to the dev!");
        return 0;

    }

}
