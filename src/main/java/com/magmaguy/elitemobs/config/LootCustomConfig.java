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
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * Created by MagmaGuy on 01/05/2017.
 */
public class LootCustomConfig {

    public void LootCustomConfig() {

        Bukkit.getLogger().info(Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getDataFolder().getAbsolutePath() + "/loot.yml");

        File file = new File(Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getDataFolder().getAbsolutePath() + "/loot.yml");

        if (!file.exists()) {

            CustomConfigConstructor customConfigConstructor = new CustomConfigConstructor("loot.yml", "loot.yml");

        }

    }

    private FileConfiguration lootConfig = null;
    private File lootConfigFile = null;
    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);
    CustomConfigLoader customConfigLoader = new CustomConfigLoader();

    public FileConfiguration getLootConfig() {

        return customConfigLoader.getCustomConfig("loot.yml");

    }

    public void reloadLootConfig(){

        customConfigLoader.reloadCustomConfig("loot.yml");

    }

}
