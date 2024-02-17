package com.magmaguy.elitemobs.worlds;

import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;

import java.io.File;
import java.util.Objects;
import java.util.logging.Filter;

public class CustomWorldLoading {

    private CustomWorldLoading() {
    }

    public static void startupWorldInitialization() {
        File folder = new File(Bukkit.getWorldContainer().getAbsolutePath());
        File[] listOfFiles = folder.listFiles();

        assert listOfFiles != null;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isDirectory() && listOfFile.getName().equals(AdventurersGuildConfig.getGuildWorldName())) {
                new InfoMessage("[EliteMobs] World " + AdventurersGuildConfig.getGuildWorldName() + " found! Loading it in...");
                Filter filter = newFilter -> false;
                Filter previousFilter = Bukkit.getLogger().getFilter();
                Bukkit.getLogger().setFilter(filter);
                try {
                    WorldCreator worldCreator = new WorldCreator(AdventurersGuildConfig.getGuildWorldName());
                    Objects.requireNonNull(Bukkit.createWorld(worldCreator)).setKeepSpawnInMemory(false);
                    new InfoMessage("[EliteMobs] World " + AdventurersGuildConfig.getGuildWorldName() +
                            " has been successfully loaded! It can be accessed through the '/ag' command, unless you changed that config option!");
                } catch (Exception ex) {
                    new WarningMessage("Failed to generate Adventurer's Guild World!");
                    ex.printStackTrace();
                }
                Bukkit.getLogger().setFilter(previousFilter);
                break;
            }
        }

    }

    public static boolean adventurersGuildWorldExists() {
        File folder = new File(Bukkit.getWorldContainer().getAbsolutePath());
        File[] listOfFiles = folder.listFiles();

        assert listOfFiles != null;
        for (File listOfFile : listOfFiles)
            if (listOfFile.isDirectory() &&
                    listOfFile.getName().equals(AdventurersGuildConfig.getGuildWorldName()))
                return true;
        return false;
    }

}
