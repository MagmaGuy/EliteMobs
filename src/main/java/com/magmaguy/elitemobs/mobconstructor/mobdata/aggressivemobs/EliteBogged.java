package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteBogged extends EliteMobProperties {

    public EliteBogged() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.BOGGED).getName();
        this.entityType = EntityType.BOGGED;
        this.defaultMaxHealth = 16;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.BOGGED).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.BOGGED).isEnabled();
        eliteMobData.add(this);
    }
}