package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.BossMobDeathCountdown;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class TimedBossMobEntity extends BossMobEntity {

    public TimedBossMobEntity(EntityType entityType, Location location, int eliteMobLevel, String name) {
        super(entityType, location, eliteMobLevel, name);
        super.setHasFarAwayUnload(false);
        super.getLivingEntity().setRemoveWhenFarAway(false);
        BossMobDeathCountdown.startDeathCountdown(super.getLivingEntity());
    }

}
