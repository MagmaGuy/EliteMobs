package com.magmaguy.elitemobs.mobconstructor;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class TrashMobEntity extends ReinforcementMobEntity {

    public TrashMobEntity(EntityType entityType, Location location, String name) {

        super(entityType, location, 1, name, CreatureSpawnEvent.SpawnReason.CUSTOM);
        super.setHasCustomHealth(true);
        super.getLivingEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(1);
        super.getLivingEntity().setHealth(1);
        super.setHasSpecialLoot(false);

    }

}
