package com.magmaguy.elitemobs.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class GuildRankData {

    private static FileConfiguration fileConfiguration;
    private static File file;

    public static FileConfiguration getFileConfiguration() {
        return fileConfiguration;
    }

    public static int getInt(String entry) {
        return fileConfiguration.getInt(entry);
    }

    public static void saveConfig() {
        try {
            fileConfiguration.save(file);
        } catch (Exception ex) {
            Bukkit.getLogger().warning("[EliteMobs] Error saving configuration " + file.getName() + "! Report this to the developer!");
        }
    }

    public static void initializeConfig() {
        file = ConfigurationEngine.fileCreator("data", "playerGuildRank.yml");
        fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
    }

}
