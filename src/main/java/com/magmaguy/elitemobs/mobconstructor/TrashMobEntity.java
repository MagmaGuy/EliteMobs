package com.magmaguy.elitemobs.mobconstructor;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

public class TrashMobEntity extends TimedBossMobEntity {

    public TrashMobEntity(LivingEntity livingEntity, String name) {

        super(livingEntity, 1, name);
        super.setHasCustomHealth(true);
        super.getLivingEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(1);
        super.getLivingEntity().setHealth(1);
        super.setHasNormalLoot(true);

    }

}
