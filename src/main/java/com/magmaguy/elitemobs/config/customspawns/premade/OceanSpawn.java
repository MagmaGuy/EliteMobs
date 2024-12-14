package com.magmaguy.elitemobs.config.customspawns.premade;

import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class OceanSpawn extends CustomSpawnConfigFields {
    public OceanSpawn() {
        super("ocean_spawn", true);
        setValidWorldEnvironments(List.of(World.Environment.NORMAL, World.Environment.CUSTOM));
        setValidBiomesStrings(new ArrayList<>(List.of(
                "OCEAN",
                "COLD_OCEAN",
                "DEEP_OCEAN",
                "FROZEN_OCEAN",
                "LUKEWARM_OCEAN",
                "DEEP_FROZEN_OCEAN",
                "DEEP_LUKEWARM_OCEAN",
                "DEEP_OCEAN")));
        setSurfaceSpawn(true);
    }
}
