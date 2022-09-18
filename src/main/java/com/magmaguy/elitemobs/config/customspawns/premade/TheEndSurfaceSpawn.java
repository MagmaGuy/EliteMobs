package com.magmaguy.elitemobs.config.customspawns.premade;

import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import org.bukkit.World;

import java.util.List;

public class TheEndSurfaceSpawn extends CustomSpawnConfigFields {
    public TheEndSurfaceSpawn() {
        super("the_end_surface_spawn", true);
        setSurfaceSpawn(true);
        setCanSpawnInLight(true);
        setValidWorldEnvironments(List.of(World.Environment.THE_END));
    }
}
