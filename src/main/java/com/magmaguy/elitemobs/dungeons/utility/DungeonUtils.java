package com.magmaguy.elitemobs.dungeons.utility;

import com.magmaguy.elitemobs.dungeons.WorldDungeonPackage;
import com.magmaguy.elitemobs.dungeons.WorldPackage;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class DungeonUtils {
    public static Pair<Integer, Integer> getLowestAndHighestLevels(List<CustomBossEntity> customBossEntities) {
        int lowestLevel = 0;
        int highestLevel = 0;

        for (CustomBossEntity customBossEntity : customBossEntities) {
            try {
                int level = customBossEntity.getLevel();
                lowestLevel = lowestLevel == 0 ? level : Math.min(lowestLevel, level);
                highestLevel = highestLevel == 0 ? level : Math.max(highestLevel, level);
            } catch (Exception ex) {
            }
        }
        return Pair.of(lowestLevel, highestLevel);
    }


    public static World loadWorld(WorldPackage worldPackage) {
        String worldName = worldPackage.getDungeonPackagerConfigFields().getWorldName();
        World.Environment environment = worldPackage.getDungeonPackagerConfigFields().getEnvironment();
        World world = loadWorld(worldName, environment);
        if (worldPackage.getDungeonPackagerConfigFields().getWormholeWorldName() != null)
            loadWorld(worldPackage.getDungeonPackagerConfigFields().getWormholeWorldName(), environment);
        if (world != null) worldPackage.setInstalled(true);
        return world;
    }

    public static World loadWorld(String worldName, World.Environment environment) {
        File folder = new File(Bukkit.getWorldContainer().getAbsolutePath());

        if (!Files.exists(Paths.get(folder.getAbsolutePath() + "/" + worldName)))
            return null;
        try {
            WorldCreator worldCreator = new WorldCreator(worldName);

            worldCreator.environment(environment);
            World world = Bukkit.createWorld(worldCreator);
            if (world != null)
                world.setKeepSpawnInMemory(false);
            world.setDifficulty(Difficulty.HARD);
            return world;
        } catch (Exception exception) {
            new WarningMessage("Failed to load packaged world " + worldName + " !");
            exception.printStackTrace();
        }
        return null;
    }

    public static boolean unloadWorld(WorldPackage worldPackage) {
        World defaultWorld = Bukkit.getWorlds().get(0);
        World wormholeWorld = null;
        if (worldPackage instanceof WorldDungeonPackage && ((WorldDungeonPackage) worldPackage).getWormholeWorld() != null)
            wormholeWorld = ((WorldDungeonPackage) worldPackage).getWormholeWorld();
        for (Player player : Bukkit.getOnlinePlayers())
            if (player.getWorld() == worldPackage.getWorld() || player.getWorld() == wormholeWorld)
                if (defaultWorld == null)
                    return false;
                else
                    player.teleport(defaultWorld.getSpawnLocation());
        Bukkit.unloadWorld(worldPackage.getWorld(), false);
        if (worldPackage instanceof WorldDungeonPackage && ((WorldDungeonPackage) worldPackage).getWormholeWorld() != null)
            Bukkit.unloadWorld(((WorldDungeonPackage) worldPackage).getWormholeWorld(), false);
        return true;
    }
}