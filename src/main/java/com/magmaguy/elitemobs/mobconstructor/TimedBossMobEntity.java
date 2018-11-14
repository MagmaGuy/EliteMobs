package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.BossMobDeathCountdown;
import org.bukkit.entity.LivingEntity;

public class TimedBossMobEntity extends BossMobEntity {

    public TimedBossMobEntity(LivingEntity livingEntity, int eliteMobLevel, String name) {
        super(livingEntity, eliteMobLevel, name);
        super.setHasFarAwayUnload(false);
        super.getLivingEntity().setRemoveWhenFarAway(false);
        BossMobDeathCountdown.startDeathCountdown(livingEntity);
    }

}
