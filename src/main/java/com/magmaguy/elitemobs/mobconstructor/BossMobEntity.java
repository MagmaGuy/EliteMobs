package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.EntityTracker;
import org.bukkit.entity.LivingEntity;

public class BossMobEntity {

    private EliteMobEntity eliteMobEntity;

    public BossMobEntity(LivingEntity livingEntity, int eliteMobLevel, String name) {
        EliteMobEntity bossMobEntity = new EliteMobEntity(livingEntity, eliteMobLevel, true);
        this.eliteMobEntity = bossMobEntity;
        bossMobEntity.setName(name);
        EntityTracker.registerNaturalEntity(livingEntity);
    }

    public EliteMobEntity getEliteMobEntity() {
        return this.eliteMobEntity;
    }

}
