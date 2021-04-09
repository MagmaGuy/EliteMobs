package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteElderGuardian extends EliteMobProperties {
    public EliteElderGuardian() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.ELDER_GUARDIAN).getName();
        this.entityType = EntityType.ELDER_GUARDIAN;
        this.defaultMaxHealth = 80;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.ELDER_GUARDIAN).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.ELDER_GUARDIAN).isEnabled();
            eliteMobData.add(this);
    }
}
