package com.magmaguy.elitemobs.worlds;

import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
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
                new InfoMessage("[EliteMobs] World " + AdventurersGuildConfig.guildWorldName + " found! Loading it in...");
                try {
                    WorldCreator worldCreator = new WorldCreator(AdventurersGuildConfig.guildWorldName);
                    Bukkit.createWorld(worldCreator).setKeepSpawnInMemory(false);
                    new InfoMessage("[EliteMobs] World " + AdventurersGuildConfig.guildWorldName + " has been successfully loaded! It can be accessed through the '/ag' command, unless you changed that config option!");
                } catch (Exception ex) {
                    new WarningMessage("Failed to generate Adventurer's Guild World!");
                    ex.printStackTrace();
                }
                break;
            }
        }

    }

    public static boolean adventurersGuildWorldExists() {
        File folder = new File(Bukkit.getWorldContainer().getAbsolutePath());
        File[] listOfFiles = folder.listFiles();

        for (File listOfFile : listOfFiles)
            if (listOfFile.isDirectory() &&
                    listOfFile.getName().equals(AdventurersGuildConfig.guildWorldName))
                return true;
        return false;
    }

}
