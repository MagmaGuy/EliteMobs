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

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.CustomConfigLoader;
import com.magmaguy.elitemobs.config.PlayerCacheConfig;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by MagmaGuy on 02/07/2017.
 */
public class OfflinePlayerCacher implements Listener {

    @EventHandler
    public void onLogin(PlayerJoinEvent event) {

        Configuration cachedConfiguration = ConfigValues.playerCacheConfig;

        String UUID = event.getPlayer().getUniqueId().toString();
        String playerName = event.getPlayer().getName();

        if (!cachedConfiguration.contains(UUID)) {

            CustomConfigLoader customConfigLoader = new CustomConfigLoader();
            customConfigLoader.getCustomConfig(PlayerCacheConfig.CONFIG_NAME).set(UUID, playerName);
            customConfigLoader.saveCustomConfig(PlayerCacheConfig.CONFIG_NAME);

            ConfigValues.initializeConfigValues();

        } else if (!cachedConfiguration.getString(UUID).equals(playerName)) {

            CustomConfigLoader customConfigLoader = new CustomConfigLoader();
            customConfigLoader.getCustomConfig(PlayerCacheConfig.CONFIG_NAME).set(UUID, playerName);
            customConfigLoader.saveCustomConfig(PlayerCacheConfig.CONFIG_NAME);

            ConfigValues.initializeConfigValues();

        }

    }

}
