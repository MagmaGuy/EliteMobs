package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.BossMobDeathCountdown;
import com.magmaguy.elitemobs.mobpowers.ElitePower;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.HashSet;

public class BossMobEntity extends EliteMobEntity {

    private byte hitsCounter = 0;

    public void incrementHitsCounter(){
        this.hitsCounter++;
        if (this.hitsCounter > 9)
            this.hitsCounter = 0;
    }

    public void resetHitsCounter(){
        this.hitsCounter = 0;
    }

    public byte getHitsCounter(){
        return this.hitsCounter;
    }

    public BossMobEntity(EntityType entityType, Location location, int eliteMobLevel, String name) {

        super(entityType, location, eliteMobLevel, name);
        setupBossMob(super.getLivingEntity());

    }

    public BossMobEntity(EntityType entityType, Location location, int eliteMobLevel, String name, HashSet<ElitePower> elitePowers) {

        super(entityType, location, eliteMobLevel, name);
        setupBossMob(super.getLivingEntity(), elitePowers);

    }

    private void setupBossMob(LivingEntity livingEntity) {

        super.setHasSpecialLoot(true);
        BossMobDeathCountdown.startDeathCountdown(livingEntity);

    }

    private void setupBossMob(LivingEntity livingEntity, HashSet<ElitePower> elitePowers) {

        setupBossMob(livingEntity);
        super.setCustomPowers(elitePowers);

    }

}
