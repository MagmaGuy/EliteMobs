package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class PlayerMaxGuildRankConfig {

    private static File file;
    public static FileConfiguration fileConfiguration;

    public static void initializeConfig() {
        file = ConfigurationEngine.fileCreator("data", "playerMaxGuildRank.yml");
        fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }

    public static void saveConfig() {
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }


}
