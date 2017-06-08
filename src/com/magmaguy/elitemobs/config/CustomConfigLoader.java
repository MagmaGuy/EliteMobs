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
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.logging.Level;

/**
 * Created by MagmaGuy on 01/05/2017.
 */
public class CustomConfigLoader {

    private FileConfiguration customConfig = null;
    private File customConfigFile = null;
    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);

    public FileConfiguration getCustomConfig(String configName) {

        if (customConfig == null) {

            reloadCustomConfig(configName);

        }

        return customConfig;
    }

    public void reloadCustomConfig(String configName){

        if (customConfigFile == null) {

            customConfigFile = new File(plugin.getDataFolder(), configName);

        }

        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

        // Look for defaults in the jar
        Reader defConfigStream = null;

        try {

            defConfigStream = new InputStreamReader(plugin.getResource(configName), "UTF8");

        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }

        if (defConfigStream != null) {

            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(customConfigFile);
            customConfig.setDefaults(defConfig);

        }

    }

    public void saveCustomDefaultConfig(String configName) {

        if (customConfig == null || customConfigFile == null) {

            return;

        }

        try {

            getCustomConfig(configName).save(customConfigFile);

        } catch (IOException ex) {

            Bukkit.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);

        }

    }

    public void saveDefaultCustomConfig(String configName) {

        if (customConfigFile == null) {

            customConfigFile = new File(plugin.getDataFolder(), configName);

        }

        if (!customConfigFile.exists()) {

            plugin.saveResource(configName, false);

        }

    }

}
