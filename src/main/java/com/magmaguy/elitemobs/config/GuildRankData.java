package com.magmaguy.elitemobs.config;

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
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }

    public static void initializeConfig() {
        file = ConfigurationEngine.fileCreator("data", "playerGuildRank.yml");
        fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }

}
