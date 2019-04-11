package com.magmaguy.elitemobs.mobconstructor;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class ReinforcementMobEntity extends EliteMobEntity {

    /*
    Half the level of the elite they're reinforcing
     */
    public ReinforcementMobEntity(EntityType entityType, Location location, int eliteMobLevel, String name, CreatureSpawnEvent.SpawnReason spawnReason) {
        super(entityType, location, (int) Math.ceil(eliteMobLevel / 2), name, spawnReason);
        super.setHasSpecialLoot(false);
    }

}
