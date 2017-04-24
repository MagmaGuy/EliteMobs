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

package com.magmaguy.magmasmobs.mobcustomizer;

import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import static org.bukkit.Bukkit.getLogger;

/**
 * Created by MagmaGuy on 18/04/2017.
 */
public class NameHandler {

    public static void customAggressiveName(Entity entity, Plugin plugin) {

        int mobLevel = entity.getMetadata("MagmasSuperMob").get(0).asInt();

        switch (entity.getType()) {
            case ZOMBIE:
                entity.setCustomName("Level " + mobLevel + " Zombie");
                break;
            case HUSK:
                entity.setCustomName("Level " + mobLevel + " Husk");
                break;
            case ZOMBIE_VILLAGER:
                entity.setCustomName("Level " + mobLevel + " Zombie Villager");
                break;
            case SKELETON:
                entity.setCustomName("Level " + mobLevel + " Skeleton");
                break;
            case WITHER_SKELETON:
                entity.setCustomName("Level " + mobLevel + " Wither Skeleton");
                break;
            case STRAY:
                entity.setCustomName("Level " + mobLevel + " Stray");
                break;
            case PIG_ZOMBIE:
                entity.setCustomName("Level " + mobLevel + " Zombie Pigman");
                break;
            case CREEPER:
                entity.setCustomName("Level " + mobLevel + " Creeper");
                break;
            case SPIDER:
                entity.setCustomName("Level " + mobLevel + " Spider");
                break;
            case ENDERMAN:
                entity.setCustomName("Level " + mobLevel + " Enderman");
                break;
            case CAVE_SPIDER:
                entity.setCustomName("Level " + mobLevel + " Cave Spider");
                break;
            case SILVERFISH:
                entity.setCustomName("Level " + mobLevel + " Silverfish");
                break;
            case BLAZE:
                entity.setCustomName("Level " + mobLevel + " Blaze");
                break;
            case WITCH:
                entity.setCustomName("Level " + mobLevel + " Witch");
                break;
            case ENDERMITE:
                entity.setCustomName("Level " + mobLevel + " Endermite");
                break;
            case POLAR_BEAR:
                entity.setCustomName("Level " + mobLevel + " Polar Bear");
                break;
            case IRON_GOLEM:
                entity.setCustomName("Level " + mobLevel + "Iron Golem");
                break;
            default:
                getLogger().info("Error: Couldn't assign custom mob name due to unexpected aggressive boss mob (talk to the dev!)");
                getLogger().info("Missing mob type: " + entity.getType());
                break;
        }

        entity.setCustomNameVisible(true);

    }


    public static void customPassiveName(Entity entity, Plugin plugin) {

        switch (entity.getType()) {

            case CHICKEN:
                entity.setCustomName("Super Chicken");
                break;
            case COW:
                entity.setCustomName("Super Cow");
                break;
            case MUSHROOM_COW:
                entity.setCustomName("Super Mooshroom");
                break;
            case PIG:
                entity.setCustomName("Super Pig");
                break;
            case SHEEP:
                entity.setCustomName("Super Sheep");
                break;
            default:
                getLogger().info("Error: Couldn't assign custom mob name due to unexpected passive boss mob (talk to the dev!)");
                getLogger().info("Missing mob type: " + entity.getType());
                break;
        }

        entity.setCustomNameVisible(true);

        entity.setMetadata("MagmasPassiveSupermob", new FixedMetadataValue(plugin, true));

    }

}
