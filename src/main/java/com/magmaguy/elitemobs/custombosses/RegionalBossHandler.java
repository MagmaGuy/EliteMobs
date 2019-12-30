package com.magmaguy.elitemobs.custombosses;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.utils.ChunkLocationChecker;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.Listener;

import java.util.HashMap;

public class RegionalBossHandler implements Listener {

    private static HashMap<CustomBossConfigFields, CustomBossEntity> livingBossEntities = new HashMap<>();

    public static void initialize() {

        for (CustomBossConfigFields customBossConfigFields : CustomBossConfigFields.getRegionalElites())
            for (World world : Bukkit.getWorlds())
                for (Chunk chunk : world.getLoadedChunks())
                    if (ChunkLocationChecker.chunkLocationCheck(customBossConfigFields.getSpawnLocation(), chunk))
                        spawnRegionalBoss(customBossConfigFields);

    }

    private static void spawnRegionalBoss(CustomBossConfigFields customBossConfigFields) {

        if (!customBossConfigFields.isEnabled()) return;
        if (customBossConfigFields.getSpawnLocation() == null) return;

        new RegionalBossEntity(customBossConfigFields);

    }

}
