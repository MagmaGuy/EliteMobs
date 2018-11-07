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

package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class EventMessage {

    public static void sendEventMessage(LivingEntity livingEntity, String configMessage) {

        String coordinates = livingEntity.getLocation().getBlockX() + ", " + livingEntity.getLocation().getBlockY() + ", " + livingEntity.getLocation().getBlockZ();
        String sendString = configMessage.replace("$location", coordinates);
        String worldName = "";

        if (livingEntity.getWorld().getName().contains("_")) {

            for (String string : livingEntity.getWorld().getName().split("_")) {

                worldName += string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase() + " ";

            }

        } else {

            worldName = livingEntity.getWorld().getName().substring(0, 1).toUpperCase() + livingEntity.getWorld().getName().substring(1).toLowerCase();

        }

        sendString = ChatColorConverter.convert(sendString.replace("$world", worldName));

        if (ConfigValues.eventsConfig.getBoolean(EventsConfig.ANNOUNCEMENT_BROADCAST_WORLD_ONLY)) {
            for (Player player : Bukkit.getServer().getOnlinePlayers())
                if (player.getWorld().equals(livingEntity.getWorld()))
                    player.sendMessage(sendString);
        } else
            Bukkit.getServer().broadcastMessage(sendString);

    }

}
