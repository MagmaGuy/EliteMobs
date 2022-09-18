package com.magmaguy.elitemobs.config.customspawns.premade;

import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.List;

public class OceanSpawn extends CustomSpawnConfigFields {
    public OceanSpawn(){
        super("ocean_spawn", true);
        setValidWorldEnvironments(List.of(World.Environment.NORMAL, World.Environment.CUSTOM));
        setValidBiomes(List.of(
                Biome.OCEAN,
                Biome.COLD_OCEAN,
                Biome.DEEP_OCEAN,
                Biome.FROZEN_OCEAN,
                Biome.LUKEWARM_OCEAN,
                Biome.DEEP_FROZEN_OCEAN,
                Biome.DEEP_LUKEWARM_OCEAN,
                Biome.DEEP_OCEAN));
        setSurfaceSpawn(true);
    }
}
