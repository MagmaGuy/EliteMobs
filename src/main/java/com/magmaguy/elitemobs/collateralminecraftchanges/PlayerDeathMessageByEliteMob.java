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

package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobcustomizer.NameHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class PlayerDeathMessageByEliteMob {

    public static void intializeDeathMessage(Player player, LivingEntity livingEntity) {

        String deathMessage;

        switch (livingEntity.getType()) {

            case BLAZE:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.BLAZE_DEATH_MESSAGE, player, livingEntity);
                break;
            case CREEPER:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.CREEPER_DEATH_MESSAGE, player, livingEntity);
                break;
            case ENDERMAN:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.ENDERMAN_DEATH_MESSAGE, player, livingEntity);
                break;
            case ENDERMITE:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.ENDERMITE_DEATH_MESSAGE, player, livingEntity);
                break;
            case IRON_GOLEM:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.IRONGOLEM_DEATH_MESSAGE, player, livingEntity);
                break;
            case POLAR_BEAR:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.POLARBEAR_DEATH_MESSAGE, player, livingEntity);
                break;
            case SILVERFISH:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.SILVERFISH_DEATH_MESSAGE, player, livingEntity);
                break;
            case SKELETON:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.SKELETON_DEATH_MESSAGE, player, livingEntity);
                break;
            case SPIDER:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.SPIDER_DEATH_MESSAGE, player, livingEntity);
                break;
            case WITCH:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.WITCH_DEATH_MESSAGE, player, livingEntity);
                break;
            case ZOMBIE:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.ZOMBIE_DEATH_MESSAGE, player, livingEntity);
                break;
            case HUSK:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.HUSK_DEATH_MESSAGE, player, livingEntity);
                break;
            case PIG_ZOMBIE:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.ZOMBIE_PIGMAN_DEATH_MESSAGE, player, livingEntity);
                break;
            case STRAY:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.STRAY_DEATH_MESSAGE, player, livingEntity);
                break;
            case WITHER_SKELETON:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.WITHER_SKELETON_DEATH_MESSAGE, player, livingEntity);
                break;
            default:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.DEFAULT_DEATH_MESSAGE, player, livingEntity);
                break;
        }

        MetadataHandler.registerMetadata(player, MetadataHandler.KILLED_BY_ELITE_MOB, true);

        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {

            /*
            Send death message
            */
            onlinePlayer.sendMessage(deathMessage);

        }

        Bukkit.getLogger().info(deathMessage);

    }

    private static String deathMessageSender(String configNode, Player player, LivingEntity livingEntity) {

        String deathMessage = ConfigValues.mobCombatSettingsConfig.getString(configNode);
        deathMessage = deathMessagePlaceholderConversion(deathMessage, player, livingEntity);

        return deathMessage;

    }

    private static String deathMessagePlaceholderConversion(String deathMessage, Player player, LivingEntity livingEntity) {

        String livingEntityName = NameHandler.customAggressiveNameIgnoresCustomName(livingEntity);

        deathMessage = deathMessage.replace("$player", player.getDisplayName());
        deathMessage = deathMessage.replace("$entity", livingEntityName);
        deathMessage = ChatColorConverter.chatColorConverter(deathMessage);

        return deathMessage;

    }

}
