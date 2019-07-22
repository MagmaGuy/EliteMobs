package com.magmaguy.elitemobs.config;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ValidWorldsConfig {

    public static FileConfiguration fileConfiguration;
    private static File file;

    public static void initializeConfig() {
        file = ConfigurationEngine.fileCreator("ValidWorlds.yml");
        fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        for (World world : Bukkit.getWorlds())
            fileConfiguration.addDefault("Valid worlds." + world.getName(), true);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

}
