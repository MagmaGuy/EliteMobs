package com.magmaguy.elitemobs.config.customspawns.premade;

import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;

public class TreasureGoblinSpawn extends CustomSpawnConfigFields {
    public TreasureGoblinSpawn() {
        super("treasure_goblin_spawn",
                true);
        setSurfaceSpawn(true);
        setValidWorldTypes(new ArrayList<>(Arrays.asList(World.Environment.NORMAL, World.Environment.CUSTOM)));
    }
}
