package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.utils.DeveloperMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ValidWorldsConfig {

    public static List<String> validWorlds = new ArrayList<>(), zoneBasedWorlds = new ArrayList<>(), nightmareWorlds = new ArrayList<>();

    public static FileConfiguration fileConfiguration;
    private static File file;

    public static void initializeConfig() {
        file = ConfigurationEngine.fileCreator("ValidWorlds.yml");
        fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);


        for (World world : Bukkit.getWorlds())
            ConfigurationEngine.setBoolean(fileConfiguration, "Valid worlds." + world.getName(), true);

        ConfigurationSection validWorldsSection = fileConfiguration.getConfigurationSection("Valid worlds");

        for (String key : validWorldsSection.getKeys(false))
            if (validWorldsSection.getBoolean(key))
                validWorlds.add(key);

        zoneBasedWorlds = ConfigurationEngine.setList(fileConfiguration, "zoneBasedWorlds", new ArrayList());
        nightmareWorlds = ConfigurationEngine.setList(fileConfiguration, "nightmareWorlds", new ArrayList());

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);

    }

    public static void addWorld(String worldName) {
        if (fileConfiguration.getKeys(true).contains("Valid worlds." + worldName)) return;

        ConfigurationEngine.setBoolean(fileConfiguration, "Valid worlds." + worldName, true);
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
        validWorlds.add(worldName);
    }

}
