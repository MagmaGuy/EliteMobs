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

package com.magmaguy.elitemobs.economy;

import com.magmaguy.elitemobs.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by MagmaGuy on 20/06/2017.
 */
public class UUIDFilter {

    public static UUID guessUUI(String string) {

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (player.getName().equalsIgnoreCase(string) || player.getDisplayName().equalsIgnoreCase(string)) {

                return player.getUniqueId();

            }

        }

        for (UUID uuid : PlayerData.playerDisplayName.keySet()) {

            if (ChatColor.stripColor(PlayerData.playerDisplayName.get(uuid)).equalsIgnoreCase(string))
                return uuid;

        }

        return null;

    }

}
