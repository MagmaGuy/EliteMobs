package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.BossMobDeathCountdown;
import com.magmaguy.elitemobs.mobpowers.majorpowers.MajorPower;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPower;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public class BossMobEntity extends EliteMobEntity {

    public BossMobEntity(LivingEntity livingEntity, int eliteMobLevel, String name) {

        super(livingEntity, eliteMobLevel);
        setupBossMob(livingEntity, eliteMobLevel, name);

    }

    public BossMobEntity(LivingEntity livingEntity, int eliteMobLevel, String name, List<MajorPower> majorPowers,
                         List<MinorPower> minorPowers) {

        super(livingEntity, eliteMobLevel);
        setupBossMob(livingEntity, eliteMobLevel, name);

    }

    private static void setupBossMob(LivingEntity livingEntity, int eliteMobLevel, String name) {

        EliteMobEntity bossMobEntity = new EliteMobEntity(livingEntity, eliteMobLevel);
        bossMobEntity.setName(name);
        EntityTracker.registerNaturalEntity(livingEntity);
        bossMobEntity.setHasStacking(false);
        bossMobEntity.setHasCustomArmor(true);
        bossMobEntity.setHasCustomPowers(true);
        BossMobDeathCountdown.startDeathCountdown(livingEntity);

    }

}
