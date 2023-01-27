package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteMagmaCube extends EliteMobProperties {
    public EliteMagmaCube() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.MAGMA_CUBE).getName();
        this.entityType = EntityType.MAGMA_CUBE;
        this.defaultMaxHealth = 16;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.MAGMA_CUBE).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.MAGMA_CUBE).isEnabled();
        eliteMobData.add(this);
    }
}
