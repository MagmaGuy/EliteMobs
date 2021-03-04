package com.magmaguy.elitemobs.dungeons.worlds;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MinidungeonWorldLoader {
    public static World loadWorld(Minidungeon minidungeon) {
        File folder = new File(Bukkit.getWorldContainer().getAbsolutePath());

        new InfoMessage("Trying to load Minidungeon world " + minidungeon.dungeonPackagerConfigFields.getWorldName());
        if (!Files.exists(Paths.get(folder.getAbsolutePath() + "/" + minidungeon.dungeonPackagerConfigFields.getWorldName())))
            return null;
        new InfoMessage("Detected Minidungeon world " + minidungeon.dungeonPackagerConfigFields.getWorldName());
        try {
            WorldCreator worldCreator = new WorldCreator(minidungeon.dungeonPackagerConfigFields.getWorldName());
            worldCreator.environment(minidungeon.dungeonPackagerConfigFields.getEnvironment());
            World world = Bukkit.createWorld(worldCreator);
            if (world != null)
                world.setKeepSpawnInMemory(false);
            new InfoMessage("Minidungeons world " + minidungeon.dungeonPackagerConfigFields.getWorldName() + " was loaded successfully!");
            minidungeon.isInstalled = true;
            if (EliteMobs.worldguardIsEnabled && minidungeon.dungeonPackagerConfigFields.getProtect())
                WorldGuardCompatibility.protectWorldMinidugeonArea(world.getSpawnLocation(), minidungeon);
            return world;
        } catch (Exception exception) {
            new WarningMessage("Failed to load Minidungeon world " + minidungeon.dungeonPackagerConfigFields.getWorldName() + " !");
        }

        return null;
    }


    public static void unloadWorld(Minidungeon minidungeon) {
        Bukkit.unloadWorld(minidungeon.dungeonPackagerConfigFields.getWorldName(), true);
    }

    public static World runtimeLoadWorld(Minidungeon minidungeon) {
        World world = loadWorld(minidungeon);
        if (world == null) return null;
        for (RegionalBossEntity regionalBossEntity : RegionalBossEntity.getRegionalBossEntitySet())
            if (regionalBossEntity.getSpawnWorldName().equals(world.getName()))
                regionalBossEntity.worldLoad();
        return world;
    }
}
