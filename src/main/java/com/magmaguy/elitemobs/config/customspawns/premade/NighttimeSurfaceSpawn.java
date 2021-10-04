package com.magmaguy.elitemobs.config.customspawns.premade;

import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;

public class NighttimeSurfaceSpawn extends CustomSpawnConfigFields {
    public NighttimeSurfaceSpawn() {
        super("nighttime_surface_spawn",
                true);
        setSurfaceSpawn(true);
        setValidWorldTypes(new ArrayList<>(Arrays.asList(World.Environment.NORMAL, World.Environment.CUSTOM)));
        setEarliestTime(12000);
        setBypassWorldGuard(false);
    }
}
