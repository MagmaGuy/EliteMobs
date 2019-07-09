package com.magmaguy.elitemobs.worlds;

import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;

import java.io.File;

public class CustomWorldLoading {

    public static void startupWorldInitialization() {
        File folder = new File(Bukkit.getWorldContainer().getAbsolutePath());
        File[] listOfFiles = folder.listFiles();

        for (File listOfFile : listOfFiles) {
            if (listOfFile.isDirectory() &&
                    listOfFile.getName().equals(AdventurersGuildConfig.guildWorldName)) {
                Bukkit.getLogger().warning("[EliteMobs] World " + AdventurersGuildConfig.guildWorldName + " found! Loading it in...");
                Bukkit.createWorld(new WorldCreator(AdventurersGuildConfig.guildWorldName));
                Bukkit.getLogger().warning("[EliteMobs] World " + AdventurersGuildConfig.guildWorldName + " has been successfully loaded! It can be accessed through the '/ag' command, unless you changed that config option!");
                break;
            }
        }

    }

}
