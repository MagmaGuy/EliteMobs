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

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class EventMessage {

    public static void sendEventMessage(String message, World world) {

        if (!ConfigValues.eventsConfig.getBoolean(EventsConfig.ANNOUNCEMENT_BROADCAST_WORLD_ONLY))
            Bukkit.getServer().broadcastMessage(message);
        else
            for (Player player : Bukkit.getServer().getOnlinePlayers())
                if (player.getWorld().equals(world))
                    player.sendMessage(message);

    }

}
