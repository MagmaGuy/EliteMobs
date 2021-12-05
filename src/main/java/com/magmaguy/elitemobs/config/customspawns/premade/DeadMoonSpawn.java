package com.magmaguy.elitemobs.config.customspawns.premade;

import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import com.magmaguy.elitemobs.events.MoonPhaseDetector;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;

public class DeadMoonSpawn extends CustomSpawnConfigFields {
    public DeadMoonSpawn() {
        super("dead_moon_spawn", true);
        setSurfaceSpawn(true);
        try {
            setValidWorldEnvironments(new ArrayList<>(Arrays.asList(World.Environment.NORMAL, World.Environment.CUSTOM)));
        } catch (Exception ex) {
            //So this happens when CUSTOM doesn't exist, which it should but in some bugged releases it doesn't.
            setValidWorldEnvironments(new ArrayList<>(Arrays.asList(World.Environment.NORMAL)));
        }
        setEarliestTime(12000);
        setMoonPhase(MoonPhaseDetector.MoonPhase.NEW_MOON);
    }
}
