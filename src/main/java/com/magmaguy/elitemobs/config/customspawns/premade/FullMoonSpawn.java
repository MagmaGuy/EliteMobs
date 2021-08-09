package com.magmaguy.elitemobs.config.customspawns.premade;

import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import com.magmaguy.elitemobs.events.MoonPhaseDetector;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;

public class FullMoonSpawn extends CustomSpawnConfigFields {
    public FullMoonSpawn(){
        super("full_moon_spawn", true);
        setSurfaceSpawn(true);
        setValidWorldTypes(new ArrayList<>(Arrays.asList(World.Environment.NORMAL, World.Environment.CUSTOM)));
        setEarliestTime(12000);
        setMoonPhase(MoonPhaseDetector.MoonPhase.FULL_MOON);
    }
}
