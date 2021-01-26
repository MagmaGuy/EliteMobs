package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Created by MagmaGuy on 01/05/2017.
 */
public class CustomConfigLoader {

    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);
    private FileConfiguration customConfig = null;
    private File customConfigFile = null;

    public FileConfiguration getCustomConfig(String configName) {
        return getCustomConfig(configName, false);
    }

    public FileConfiguration getCustomConfig(String configName, boolean isDataFile) {

        if (customConfig == null) {

            reloadCustomConfig(configName, isDataFile);

        }

        return customConfig;
    }

    public void reloadCustomConfig(String configName, boolean isDataFile) {

        if (customConfigFile == null && !isDataFile)
            customConfigFile = new File(plugin.getDataFolder(), configName);

        if (customConfigFile == null && isDataFile) {
            customConfigFile = new File(plugin.getDataFolder(), "data/" + configName);
            if (!customConfigFile.exists()) customConfigFile.getParentFile().mkdirs();
        }

        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

        // Look for defaults in the jar
        Reader defConfigStream = null;

        defConfigStream = new InputStreamReader(plugin.getResource(configName), StandardCharsets.UTF_8);

        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(customConfigFile);
            customConfig = defConfig;
        }

    }

    public void saveCustomConfig(String configName) {

        if (customConfig == null || customConfigFile == null)
            return;

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
