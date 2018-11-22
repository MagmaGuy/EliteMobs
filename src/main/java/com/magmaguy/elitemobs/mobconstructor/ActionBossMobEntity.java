package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.mobpowers.ElitePower;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.HashSet;

public class ActionBossMobEntity extends BossMobEntity {

    /*
    These bosses can be removed when a chunk unloads
     */
    public ActionBossMobEntity(EntityType entityType, Location location, int eliteMobLevel, String name) {
        super(entityType, location, eliteMobLevel, name);
    }

    public ActionBossMobEntity(EntityType entityType, Location location, int eliteMobsLevel, String name, HashSet<ElitePower> elitePowers) {
        super(entityType, location, eliteMobsLevel, name, elitePowers);
    }

}
