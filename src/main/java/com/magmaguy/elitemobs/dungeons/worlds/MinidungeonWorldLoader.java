package com.magmaguy.elitemobs.dungeons.worlds;

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

        new InfoMessage("Trying to load Minidungeon world " + minidungeon.getDungeonPackagerConfigFields().getWorldName());
        if (!Files.exists(Paths.get(folder.getAbsolutePath() + "/" + minidungeon.getDungeonPackagerConfigFields().getWorldName())))
            return null;
        new InfoMessage("Detected Minidungeon world " + minidungeon.getDungeonPackagerConfigFields().getWorldName());
        try {
            WorldCreator worldCreator = new WorldCreator(minidungeon.getDungeonPackagerConfigFields().getWorldName());
            worldCreator.environment(minidungeon.getDungeonPackagerConfigFields().getEnvironment());
            World world = Bukkit.createWorld(worldCreator);
            if (world != null)
                world.setKeepSpawnInMemory(false);
            new InfoMessage("Minidungeons world " + minidungeon.getDungeonPackagerConfigFields().getWorldName() + " was loaded successfully!");
            minidungeon.setInstalled(true);
            //if (EliteMobs.worldGuardIsEnabled && minidungeon.dungeonPackagerConfigFields.isProtect())
            //    WorldGuardCompatibility.protectWorldMinidugeonArea(world.getSpawnLocation(), minidungeon);
            return world;
        } catch (Exception exception) {
            new WarningMessage("Failed to load Minidungeon world " + minidungeon.getDungeonPackagerConfigFields().getWorldName() + " !");
        }

        return null;
    }


    public static void unloadWorld(Minidungeon minidungeon) {
        Bukkit.unloadWorld(minidungeon.getDungeonPackagerConfigFields().getWorldName(), true);
    }

    public static World runtimeLoadWorld(Minidungeon minidungeon) {
        World world = loadWorld(minidungeon);
        if (world == null) return null;
        //for (RegionalBossEntity regionalBossEntity : RegionalBossEntity.getRegionalBossEntitySet())
        //    if (regionalBossEntity.getWorldName().equals(world.getName()))
        //        regionalBossEntity.worldLoad();
        return world;
    }
}
