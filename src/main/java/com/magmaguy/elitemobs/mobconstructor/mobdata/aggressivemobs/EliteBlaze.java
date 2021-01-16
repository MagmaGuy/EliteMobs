package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteBlaze extends EliteMobProperties {

    public EliteBlaze() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.BLAZE).getName();
        this.entityType = EntityType.BLAZE;
        this.defaultMaxHealth = 20;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.BLAZE).getBaseDamage();
        super.addMajorPower("tracking_fireball.yml");
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.BLAZE).isEnabled();
        if (this.isEnabled)
            eliteMobData.add(this);
    }

}
