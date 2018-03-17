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
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class PlayerDeathMessageByEliteMob {

    public static void handleDeathMessage(Player player, LivingEntity livingEntity) {

        player.setMetadata(MetadataHandler.KILLED_BY_ELITE_MOB, new FixedMetadataValue(MetadataHandler.PLUGIN, true));

        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {

            /*
            Send death message
            */
            String deathMessage = ConfigValues.mobCombatSettingsConfig.getString(MobCombatSettingsConfig.PLAYER_DEATH_MESSAGE);
            deathMessage = deathMessage.replace("$player", player.getDisplayName());
            deathMessage = deathMessage.replace("$entity", livingEntity.getCustomName());
            deathMessage = ChatColorConverter.chatColorConverter(deathMessage);

            onlinePlayer.sendMessage(deathMessage);

        }

    }

}
