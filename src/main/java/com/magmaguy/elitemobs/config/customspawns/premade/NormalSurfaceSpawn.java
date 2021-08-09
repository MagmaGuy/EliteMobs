package com.magmaguy.elitemobs.config.customspawns.premade;

import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;

public class NormalSurfaceSpawn extends CustomSpawnConfigFields {
    public NormalSurfaceSpawn() {
        super("normal_surface_spawn",
                true);
        setSurfaceSpawn(true);
        setValidWorldTypes(new ArrayList<>(Arrays.asList(World.Environment.NORMAL, World.Environment.CUSTOM)));
        setBypassWorldGuard(false);
    }
}
