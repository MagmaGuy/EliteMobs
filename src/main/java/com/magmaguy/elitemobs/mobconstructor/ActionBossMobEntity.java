package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.mobpowers.ElitePower;
import org.bukkit.entity.LivingEntity;

import java.util.HashSet;

public class ActionBossMobEntity extends BossMobEntity {

    /*
    These bosses can be removed when a chunk unloads
     */
    public ActionBossMobEntity(LivingEntity livingEntity, int eliteMobLevel, String name) {
        super(livingEntity, eliteMobLevel, name);
    }

    public ActionBossMobEntity(LivingEntity livingEntity, int eliteMobsLevel, String name, HashSet<ElitePower> elitePowers) {
        super(livingEntity, eliteMobsLevel, name, elitePowers);
    }

}
