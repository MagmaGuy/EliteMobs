package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteWolf extends EliteMobProperties {

    public EliteWolf() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.WOLF).getName();
        this.entityType = EntityType.WOLF;
        this.defaultMaxHealth = 8;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.WOLF).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.WOLF).isEnabled();
        if (this.isEnabled)
            eliteMobData.add(this);
    }

}
