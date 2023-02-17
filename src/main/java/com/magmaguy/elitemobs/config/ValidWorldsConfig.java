package com.magmaguy.elitemobs.config;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ValidWorldsConfig {
    @Getter
    private static final List<String> validWorlds = new ArrayList<>();
    @Getter
    private static List<String> zoneBasedWorlds = new ArrayList<>();
    @Getter
    private static List<String> nightmareWorlds = new ArrayList<>();
    @Getter
    private static FileConfiguration fileConfiguration;
    private static File file;

    private ValidWorldsConfig() {
    }

    public static void initializeConfig() {
        file = ConfigurationEngine.fileCreator("ValidWorlds.yml");
        fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        for (World world : Bukkit.getWorlds())
            ConfigurationEngine.setBoolean(fileConfiguration, "Valid worlds." + world.getName(), true);

        ConfigurationSection validWorldsSection = fileConfiguration.getConfigurationSection("Valid worlds");

        for (String key : validWorldsSection.getKeys(false))
            if (validWorldsSection.getBoolean(key))
                validWorlds.add(key);

        zoneBasedWorlds = ConfigurationEngine.setList(
                List.of("Sets the list of zone-based worlds.",
                        "THE ZONE-BASED GAME MODE IS OUTDATED AND WILL SOON BE REMOVED!"),
                file, fileConfiguration, "zoneBasedWorlds", new ArrayList(), false);
        nightmareWorlds = ConfigurationEngine.setList(
                List.of("Sets the list of nightmare mode worlds.",
                        "Nightmare mode worlds are a game mode where days are shorter and players can not sleep.",
                        "Nightmare worlds also have higher amounts of elite spawns.",
                        "https://github.com/MagmaGuy/EliteMobs/wiki/%5BGame-Mode%5D-Nightmare-mode"),
                file, fileConfiguration, "nightmareWorlds", new ArrayList(), false);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);

    }

    public static void addWorld(String worldName) {
        if (fileConfiguration.getKeys(true).contains("Valid worlds." + worldName)) return;

        ConfigurationEngine.setBoolean(
                List.of("Sets if elites will spawn in this world."),
                fileConfiguration, "Valid worlds." + worldName, true);
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
        validWorlds.add(worldName);
    }

}
