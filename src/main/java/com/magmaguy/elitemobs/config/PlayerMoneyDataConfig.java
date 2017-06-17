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

package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class PlayerMoneyDataConfig {

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();

    public void intializeEconomySettingsConfig () {

        getEconomySettingsConfig().options().copyDefaults(true);
        saveDefaultCustomConfig();
        saveCustomConfig();

    }

    public FileConfiguration getEconomySettingsConfig() {

        return customConfigLoader.getCustomConfig("playerMoneyData.yml");

    }

    public void reloadCustomConfig() {

        customConfigLoader.reloadCustomConfig("playerMoneyData.yml");

    }

    public void saveCustomConfig() {

        customConfigLoader.saveCustomDefaultConfig("playerMoneyData.yml");

    }

    public void saveDefaultCustomConfig() {

        customConfigLoader.saveDefaultCustomConfig("playerMoneyData.yml");

    }

}
