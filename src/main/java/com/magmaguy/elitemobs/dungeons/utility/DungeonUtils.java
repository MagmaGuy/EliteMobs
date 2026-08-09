package com.magmaguy.elitemobs.dungeons.utility;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.dungeons.WorldDungeonPackage;
import com.magmaguy.elitemobs.dungeons.WorldPackage;
import com.magmaguy.elitemobs.mobconstructor.PersistentObjectHandler;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.TemporaryWorldManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

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
        String worldName = worldPackage.getContentPackagesConfigFields().getWorldName();
        World.Environment environment = worldPackage.getContentPackagesConfigFields().getEnvironment();
        World world = loadWorld(worldName, environment, worldPackage.getContentPackagesConfigFields());
        if (worldPackage.getContentPackagesConfigFields().getWormholeWorldName() != null)
            loadWorld(worldPackage.getContentPackagesConfigFields().getWormholeWorldName(), environment, worldPackage.getContentPackagesConfigFields());
        if (world != null) worldPackage.setInstalled(true);
        return world;
    }

    public static World loadWorld(String worldName, World.Environment environment, ContentPackagesConfigFields contentPackagesConfigFields) {
        World world = TemporaryWorldManager.loadVoidTemporaryWorld(worldName, environment);
        if (world != null) {
            EliteMobsWorld.create(world.getUID(), contentPackagesConfigFields);
            //Persistent objects that were parsed while this world was unloaded are filed under its name and are only
            //drained by WorldLoadEvent. TemporaryWorldManager returns an already-loaded world without firing that
            //event, which leaves the handlers stranded, so drain explicitly. The drain is idempotent: handlers that
            //the event already moved are keyed by chunk hash and this pass finds an empty bucket for the world name.
            PersistentObjectHandler.loadWorld(world);
        }
        return world;
    }

    public static boolean unloadWorld(WorldPackage worldPackage) {
        World packageWorld = worldPackage.getWorld();
        World wormholeWorld = worldPackage instanceof WorldDungeonPackage worldDungeonPackage
                ? worldDungeonPackage.getWormholeWorld()
                : null;

        World fallbackWorld = Bukkit.getWorlds().stream()
                .filter(world -> world != packageWorld && world != wormholeWorld)
                .findFirst()
                .orElse(null);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld() != packageWorld && player.getWorld() != wormholeWorld) continue;
            if (fallbackWorld == null || !player.teleport(fallbackWorld.getSpawnLocation())) {
                Logger.warn("Could not move " + player.getName() + " out of dungeon world " +
                        player.getWorld().getName() + "; refusing to unload it.");
                return false;
            }
        }

        if (!unloadRegisteredWorld(packageWorld, worldPackage.getContentPackagesConfigFields())) return false;
        return wormholeWorld == packageWorld ||
                unloadRegisteredWorld(wormholeWorld, worldPackage.getContentPackagesConfigFields());
    }

    private static boolean unloadRegisteredWorld(World world,
                                                  ContentPackagesConfigFields contentPackagesConfigFields) {
        if (world == null) return true;

        EliteMobsWorld.destroy(world.getUID());
        if (Bukkit.unloadWorld(world, false)) return true;

        // Keep protection state accurate when Bukkit refuses the unload.
        EliteMobsWorld.create(world.getUID(), contentPackagesConfigFields);
        Logger.warn("Bukkit refused to unload dungeon world " + world.getName() + ".");
        return false;
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
