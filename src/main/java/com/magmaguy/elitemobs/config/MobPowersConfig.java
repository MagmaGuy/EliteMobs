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
import org.bukkit.configuration.Configuration;

/**
 * Created by MagmaGuy on 01/05/2017.
 */
public class MobPowersConfig {

    public static final String CONFIG_NAME = "MobPowers.yml";
    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    private Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        for (String string : MetadataHandler.defensivePowerList) {

            configuration.addDefault("Powers.Defensive Powers." + string, true);

        }

        for (String string : MetadataHandler.offensivePowerList) {

            configuration.addDefault("Powers.Offensive Powers." + string, true);

        }

        for (String string : MetadataHandler.miscellaneousPowerList) {

            configuration.addDefault("Powers.Miscellaneous Powers." + string, true);

        }

        for (String string : MetadataHandler.majorPowerList) {

            configuration.addDefault("Powers.Major Powers." + string, true);

        }

        customConfigLoader.getCustomConfig(CONFIG_NAME).options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
