package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.mobpowers.ElitePower;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.HashSet;

public class ActionBossMobEntity extends BossMobEntity {

    /*
    These bosses can be removed when a chunk unloads
     */
    public ActionBossMobEntity(EntityType entityType, Location location, int eliteMobLevel, String name, CreatureSpawnEvent.SpawnReason spawnReason) {
        super(entityType, location, eliteMobLevel, name, spawnReason);
    }

    public ActionBossMobEntity(EntityType entityType, Location location, int eliteMobsLevel, String name, HashSet<ElitePower> elitePowers, CreatureSpawnEvent.SpawnReason spawnReason) {
        super(entityType, location, eliteMobsLevel, name, elitePowers, spawnReason);
    }

}
