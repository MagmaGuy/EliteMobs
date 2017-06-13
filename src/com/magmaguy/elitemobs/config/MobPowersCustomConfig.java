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

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by MagmaGuy on 01/05/2017.
 */
public class MobPowersCustomConfig {

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();

    public void initializeMobPowersConfig () {

        for (String string : MetadataHandler.minorPowerList()) {

            this.getMobPowersConfig().addDefault("Powers.Minor Powers." + string, true);

        }

        for (String string : MetadataHandler.majorPowerList()) {

            this.getMobPowersConfig().addDefault("Powers.Major Powers." + string, true);

        }

        getMobPowersConfig().options().copyDefaults(true);
        saveDefaultCustomConfig();
        saveCustomConfig();

    }

    public FileConfiguration getMobPowersConfig() {

        return customConfigLoader.getCustomConfig("mobPowers.yml");

    }

    public void reloadCustomConfig() {

        customConfigLoader.reloadCustomConfig("mobPowers.yml");

    }

    public void saveCustomConfig() {

        customConfigLoader.saveCustomDefaultConfig("mobPowers.yml");

    }

    public void saveDefaultCustomConfig() {

        customConfigLoader.saveDefaultCustomConfig("mobPowers.yml");

    }

}
