package com.magmaguy.elitemobs.mobconstructor;

import org.bukkit.entity.LivingEntity;

public class ReinforcementMobEntity extends EliteMobEntity {

    /*
    Half the level of the elite they're reinforcing
     */
    public ReinforcementMobEntity(LivingEntity livingEntity, int eliteMobLevel, String name) {
        super(livingEntity, (int) Math.ceil(eliteMobLevel / 2));
        super.setName(name);
        super.setHasCustomName(true);
        super.setHasStacking(false);
        super.setHasCustomArmor(true);
        super.setHasCustomPowers(true);
        super.setHasNormalLoot(true);
    }

}
