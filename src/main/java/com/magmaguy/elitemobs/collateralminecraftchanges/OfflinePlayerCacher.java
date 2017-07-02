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

import com.magmaguy.elitemobs.config.PlayerCacheConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by MagmaGuy on 02/07/2017.
 */
public class OfflinePlayerCacher implements Listener {

    private static PlayerCacheConfig playerCacheConfig = new PlayerCacheConfig();
    private static FileConfiguration configuration = playerCacheConfig.getPlayerCacheConfig();

    @EventHandler
    public static void onLogin (PlayerJoinEvent event) {

        String UUID = event.getPlayer().getUniqueId().toString();
        String playerName = event.getPlayer().getName();

        if (!configuration.contains(UUID)) {

            configuration.set(UUID, playerName);
            Bukkit.getLogger().info("test1");

            playerCacheConfig.saveCustomConfig();

        } else if (!configuration.getString(UUID).equals(playerName)) {

            configuration.set(UUID, playerName);
            Bukkit.getLogger().info("test2");

            playerCacheConfig.saveCustomConfig();

        }

    }

}
