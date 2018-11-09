package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.mobpowers.majorpowers.MajorPower;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPower;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public class ActionBossMobEntity extends BossMobEntity {

    /*
    These bosses can be removed when a chunk unloads
     */
    public ActionBossMobEntity(LivingEntity livingEntity, int eliteMobLevel, String name) {
        super(livingEntity, eliteMobLevel, name);
    }

    public ActionBossMobEntity(LivingEntity livingEntity, int eliteMobsLevel, String name, List<MajorPower> majorPowers,
                               List<MinorPower> minorPowers) {
        super(livingEntity, eliteMobsLevel, name, majorPowers, minorPowers);
    }

}
