package com.magmaguy.elitemobs.config;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ValidWorldsConfig {

    public static List<String> zoneBasedWorlds, nightmareWorlds = new ArrayList<>();

    public static FileConfiguration fileConfiguration;
    private static File file;

    public static void initializeConfig() {
        file = ConfigurationEngine.fileCreator("ValidWorlds.yml");
        fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        for (World world : Bukkit.getWorlds())
            fileConfiguration.addDefault("Valid worlds." + world.getName(), true);

        zoneBasedWorlds = ConfigurationEngine.setList(fileConfiguration, "zoneBasedWorlds", new ArrayList());
        nightmareWorlds = ConfigurationEngine.setList(fileConfiguration, "nightmareWorlds", new ArrayList());

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);

    }

}
