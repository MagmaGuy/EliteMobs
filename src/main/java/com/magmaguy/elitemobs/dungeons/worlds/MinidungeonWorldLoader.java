package com.magmaguy.elitemobs.dungeons.worlds;

import com.magmaguy.elitemobs.dungeons.Minidungeon;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MinidungeonWorldLoader {
    public static World loadWorld(Minidungeon minidungeon) {
        return loadWorld(minidungeon, minidungeon.getDungeonPackagerConfigFields().getWorldName(), minidungeon.getDungeonPackagerConfigFields().getEnvironment());
    }

    public static World loadWorld(Minidungeon minidungeon, String worldName, World.Environment environment) {
        File folder = new File(Bukkit.getWorldContainer().getAbsolutePath());

        new InfoMessage("Trying to load Minidungeon world " + worldName);
        if (!Files.exists(Paths.get(folder.getAbsolutePath() + "/" + worldName)))
            return null;
        new InfoMessage("Detected Minidungeon world " + worldName);
        try {
            WorldCreator worldCreator = new WorldCreator(worldName);
            worldCreator.environment(environment);
            World world = Bukkit.createWorld(worldCreator);
            if (world != null)
                world.setKeepSpawnInMemory(false);
            new InfoMessage("Minidungeons world " + worldName + " was loaded successfully!");
            minidungeon.setInstalled(true);
            world.setDifficulty(Difficulty.HARD);
            return world;
        } catch (Exception exception) {
            new WarningMessage("Failed to load Minidungeon world " + worldName + " !");
            exception.printStackTrace();
        }

        return null;
    }

    public static void unloadWorld(Minidungeon minidungeon) {
        Bukkit.unloadWorld(minidungeon.getWorld(), true);
        if (minidungeon.getWormholeWorld() != null) Bukkit.unloadWorld(minidungeon.getWormholeWorld(), true);
    }
}
