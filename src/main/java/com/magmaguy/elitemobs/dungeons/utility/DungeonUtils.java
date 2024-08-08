package com.magmaguy.elitemobs.dungeons.utility;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.dungeons.WorldDungeonPackage;
import com.magmaguy.elitemobs.dungeons.WorldPackage;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Filter;

public class DungeonUtils {
    public static Pair getLowestAndHighestLevels(List<CustomBossEntity> customBossEntities) {
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
        return new Pair(lowestLevel, highestLevel);
    }

    public static World loadWorld(WorldPackage worldPackage) {
        String worldName = worldPackage.getDungeonPackagerConfigFields().getWorldName();
        World.Environment environment = worldPackage.getDungeonPackagerConfigFields().getEnvironment();
        World world = loadWorld(worldName, environment, worldPackage.getDungeonPackagerConfigFields());
        if (worldPackage.getDungeonPackagerConfigFields().getWormholeWorldName() != null)
            loadWorld(worldPackage.getDungeonPackagerConfigFields().getWormholeWorldName(), environment, worldPackage.getDungeonPackagerConfigFields());
        if (world != null) worldPackage.setInstalled(true);
        return world;
    }

    public static World loadWorld(String worldName, World.Environment environment, DungeonPackagerConfigFields dungeonPackagerConfigFields) {
        if (Bukkit.getWorld(worldName) != null) {
            EliteMobsWorld.create(Bukkit.getWorld(worldName).getUID(), dungeonPackagerConfigFields);
            return Bukkit.getWorld(worldName);
        }

        File folder = new File(Bukkit.getWorldContainer().getAbsolutePath());

        if (!Files.exists(Paths.get(folder.getAbsolutePath() + File.separatorChar + worldName))) {
            Logger.warn("File  " + folder.getAbsolutePath() + File.separatorChar + worldName + " does not exist!");
            return null;
        }

        Logger.info("Loading world " + worldName + " !");

        Filter filter = newFilter -> false;

        Filter previousFilter = Bukkit.getLogger().getFilter();

        Bukkit.getLogger().setFilter(filter);

        try {
            WorldCreator worldCreator = new WorldCreator(worldName);
            worldCreator.environment(environment);
            worldCreator.keepSpawnInMemory(false);
            World world = Bukkit.createWorld(worldCreator);
            if (world != null) world.setKeepSpawnInMemory(false);
            world.setDifficulty(Difficulty.HARD);
            world.setAutoSave(false);
            Bukkit.getLogger().setFilter(previousFilter);

            EliteMobsWorld.create(world.getUID(), dungeonPackagerConfigFields);

            return world;
        } catch (Exception exception) {
            Bukkit.getLogger().setFilter(previousFilter);
            Logger.warn("Failed to load world " + worldName + " !");
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

        EliteMobsWorld.destroy(worldPackage.getWorld().getUID());

        return true;
    }

    public static class Pair {
        @Getter
        Integer lowestValue;
        @Getter
        Integer highestValue;

        public Pair(Integer lowestValue, Integer highestValue) {
            this.lowestValue = lowestValue;
            this.highestValue = highestValue;
        }
    }
}