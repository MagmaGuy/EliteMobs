package com.magmaguy.elitemobs.config.customspawns.premade;

import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NormalSurfaceSpawn extends CustomSpawnConfigFields {
    public NormalSurfaceSpawn() {
        super("normal_surface_spawn",
                true);
        setSurfaceSpawn(true);
        setCanSpawnInLight(true);
        try {
            setValidWorldEnvironments(new ArrayList<>(Arrays.asList(World.Environment.NORMAL, World.Environment.CUSTOM)));
        } catch (NoSuchFieldError ex) {
            //So this happens when CUSTOM doesn't exist, which it should but in some bugged releases it doesn't.
            setValidWorldEnvironments(new ArrayList<>(List.of(World.Environment.NORMAL)));
        }
        setBypassWorldGuard(false);
    }
}
