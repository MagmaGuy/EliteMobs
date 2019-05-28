package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class EliteConfigGenerator {

    public static File getFile(String path, String fileName) {
        File file = new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/" + path + "/", fileName);
        return getFile(file);
    }

    public static File getFile(String fileName) {
        File file = new File(MetadataHandler.PLUGIN.getDataFolder().getPath(), fileName);
        return getFile(file);
    }

    public static File getFile(File file) {
        if (!file.exists())
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException ex) {
                Bukkit.getLogger().warning("[EliteMobs] Error generating the plugin file: " + file.getName());
            }

        return file;
    }

    public static FileConfiguration getFileConfiguration(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void saveDefaults(File file, FileConfiguration fileConfiguration) {
        fileConfiguration.options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(fileConfiguration);
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
