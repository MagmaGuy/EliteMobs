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

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import static com.magmaguy.elitemobs.ChatColorConverter.chatColorConverter;
import static org.bukkit.Bukkit.getLogger;

/**
 * Created by MagmaGuy on 18/04/2017.
 */
public class NameHandler {

    private static final Configuration TRANSLATION_CONFIG = ConfigValues.translationConfig;

    public static String customAggressiveName(Entity entity) {

        if (entity.hasMetadata(MetadataHandler.CUSTOM_NAME)) {

            return null;

        }

        int mobLevel = entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt();

        switch (entity.getType()) {
            case ZOMBIE:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Zombie").get(0)) + mobLevel
                        + (chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Zombie").get(1))));
                break;
            case HUSK:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Husk").get(0)) + mobLevel
                        + chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Husk").get(1)));
                break;
            case ZOMBIE_VILLAGER:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Zombie").get(0)) + mobLevel
                        + chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Zombie").get(1)));
                //TODO: separate zombievillager from zombie
                break;
            case SKELETON:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Skeleton").get(0)) + mobLevel
                        + chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Skeleton").get(1)));
                break;
            case WITHER_SKELETON:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.WitherSkeleton").get(0)) + mobLevel
                        + chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.WitherSkeleton").get(1)));
                break;
            case STRAY:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Stray").get(0)) + mobLevel
                        + chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Stray").get(1)));
                break;
            case PIG_ZOMBIE:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.PigZombie").get(0)) + mobLevel
                        + chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.PigZombie").get(1)));
                break;
            case CREEPER:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Creeper").get(0)) + mobLevel
                        + chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Creeper").get(1)));
                break;
            case SPIDER:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Spider").get(0)) + mobLevel
                        + chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Spider").get(1)));
                break;
            case ENDERMAN:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Enderman").get(0)) + mobLevel
                        + chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Enderman").get(1)));
                break;
            case CAVE_SPIDER:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.CaveSpider").get(0)) + mobLevel
                        + chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.CaveSpider").get(1)));
                break;
            case SILVERFISH:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Silverfish").get(0)) + mobLevel
                        + chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Silverfish").get(1)));
                break;
            case BLAZE:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Blaze").get(0)) + mobLevel
                        + TRANSLATION_CONFIG.getStringList("Elite Mob Names.Blaze").get(1));
                break;
            case WITCH:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Witch").get(0)) + mobLevel
                        + chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Witch").get(1)));
                break;
            case ENDERMITE:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Endermite").get(0)) + mobLevel
                        + chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.Endermite").get(1)));
                break;
            case POLAR_BEAR:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.PolarBear").get(0)) + mobLevel
                        + chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.PolarBear").get(1)));
                break;
            case IRON_GOLEM:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.IronGolem").get(0)) + mobLevel
                        + chatColorConverter(TRANSLATION_CONFIG.getStringList("Elite Mob Names.IronGolem").get(1)));
                break;
            default:
                getLogger().info("Error: Couldn't assign custom mob name due to unexpected aggressive boss mob (talk to the dev!)");
                getLogger().info("Missing mob type: " + entity.getType());
                break;
        }

        entity.setCustomNameVisible(true);
        return entity.getCustomName();

    }


    public static void customPassiveName(Entity entity, Plugin plugin) {

        switch (entity.getType()) {

            case CHICKEN:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getString("Elite Mob Names.Chicken")));
                break;
            case COW:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getString("Elite Mob Names.Cow")));
                break;
            case MUSHROOM_COW:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getString("Elite Mob Names.MushroomCow")));
                break;
            case PIG:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getString("Elite Mob Names.Pig")));
                break;
            case SHEEP:
                entity.setCustomName(chatColorConverter(TRANSLATION_CONFIG.getString("Elite Mob Names.Sheep")));
                break;
            default:
                getLogger().info("Error: Couldn't assign custom mob name due to unexpected passive boss mob (talk to the dev!)");
                getLogger().info("Missing mob type: " + entity.getType());
                break;
        }

        entity.setCustomNameVisible(true);

        entity.setMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD, new FixedMetadataValue(plugin, true));

    }

}
