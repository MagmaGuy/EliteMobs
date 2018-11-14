package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.BossMobDeathCountdown;
import com.magmaguy.elitemobs.mobpowers.ElitePower;
import org.bukkit.entity.LivingEntity;

import java.util.HashSet;

public class BossMobEntity extends EliteMobEntity {

    public BossMobEntity(LivingEntity livingEntity, int eliteMobLevel, String name) {

        super(livingEntity, eliteMobLevel, name);
        setupBossMob(livingEntity);

    }

    public BossMobEntity(LivingEntity livingEntity, int eliteMobLevel, String name, HashSet<ElitePower> elitePowers) {

        super(livingEntity, eliteMobLevel, name);
        setupBossMob(livingEntity, elitePowers);

    }

    private void setupBossMob(LivingEntity livingEntity) {

        super.setHasStacking(false);
        super.setHasCustomArmor(true);
        super.setHasCustomPowers(true);
        super.setHasNormalLoot(true);
        BossMobDeathCountdown.startDeathCountdown(livingEntity);

    }

    private void setupBossMob(LivingEntity livingEntity, HashSet<ElitePower> elitePowers) {

        setupBossMob(livingEntity);
        super.setCustoMPowers(elitePowers);

    }

}
