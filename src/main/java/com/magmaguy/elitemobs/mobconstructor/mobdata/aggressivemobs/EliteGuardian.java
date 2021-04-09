package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteGuardian extends EliteMobProperties {
    public EliteGuardian() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.GUARDIAN).getName();
        this.entityType = EntityType.GUARDIAN;
        this.defaultMaxHealth = 30;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.GUARDIAN).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.GUARDIAN).isEnabled();
            eliteMobData.add(this);
    }
}
