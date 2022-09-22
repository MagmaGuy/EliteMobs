package com.magmaguy.elitemobs.config.customspawns.premade;

import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import org.bukkit.World;

import java.util.List;

public class NetherSpawn extends CustomSpawnConfigFields {
    public NetherSpawn() {
        super("nether_spawn", true);
        setCanSpawnInLight(true);
        setValidWorldEnvironments(List.of(World.Environment.NETHER));
        setUndergroundSpawn(true);
    }
}
