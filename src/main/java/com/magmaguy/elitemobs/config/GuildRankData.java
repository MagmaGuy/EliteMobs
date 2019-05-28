package com.magmaguy.elitemobs.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;

public class GuildRankData {

    private static FileConfiguration fileConfiguration;
    private static File file;

    public static FileConfiguration getFileConfiguration() {
        return fileConfiguration;
    }

    public static boolean getBoolean(String entry) {
        return fileConfiguration.getBoolean(entry);
    }

    public static String getString(String entry) {
        return fileConfiguration.getString(entry);
    }

    public static List<String> getStringList(String entry) {
        return fileConfiguration.getStringList(entry);
    }

    public static int getInt(String entry) {
        return fileConfiguration.getInt(entry);
    }

    public static double getDouble(String entry) {
        return fileConfiguration.getDouble(entry);
    }

    public static File getFile() {
        return file;
    }

    public static void saveConfig() {
        try {
            fileConfiguration.save(file);
        } catch (Exception ex) {
            Bukkit.getLogger().warning("[EliteMobs] Error saving configuration " + file.getName() + "! Report this to the developer!");
        }
    }

    public static void initializeConfig() {
        file = EliteConfigGenerator.getFile("data", "playerGuildRank.yml");
        fileConfiguration = EliteConfigGenerator.getFileConfiguration(file);
    }

}
