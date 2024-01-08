package com.magmaguy.elitemobs.config.customspawns.premade;

import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NormalUndergroundSpawn extends CustomSpawnConfigFields {
    public NormalUndergroundSpawn() {
        super("normal_underground_spawn",
                true);
        setSurfaceSpawn(false);
        setCanSpawnInLight(false);
        try {
            setValidWorldEnvironments(new ArrayList<>(Arrays.asList(World.Environment.NORMAL, World.Environment.CUSTOM)));
        } catch (NoSuchFieldError ex) {
            //So this happens when CUSTOM doesn't exist, which it should but in some bugged releases it doesn't.
            setValidWorldEnvironments(new ArrayList<>(List.of(World.Environment.NORMAL)));
        }
        setBypassWorldGuard(false);
    }
}
