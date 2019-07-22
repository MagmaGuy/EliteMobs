package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

/**
 * Created by MagmaGuy on 02/07/2017.
 */
public class PlayerCacheDataConfig {

    private static File file;
    public static FileConfiguration fileConfiguration;

    public static void initializeConfig() {
        file = ConfigurationEngine.fileCreator("data", "playerCache.yml");
        fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }

    public static void saveConfig() {
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }

}
