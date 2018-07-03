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

import org.bukkit.configuration.Configuration;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class PlayerMoneyData {

    public static final String CONFIG_NAME = "playerMoneyData.yml";
    public CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    public Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME, true);

    public void intializeConfig() {

        //no real defaults, just a data file
        customConfigLoader.getCustomConfig(CONFIG_NAME, true).options().copyDefaults(true);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME, true);
        customConfigLoader.saveCustomConfig(CONFIG_NAME, true);

    }

}
