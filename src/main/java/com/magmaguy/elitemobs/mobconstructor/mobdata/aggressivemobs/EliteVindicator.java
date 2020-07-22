package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteVindicator extends EliteMobProperties {
    public EliteVindicator() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.VINDICATOR).getName();
        this.entityType = EntityType.VINDICATOR;
        this.defaultMaxHealth = 24;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.VINDICATOR).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.VINDICATOR).isEnabled();
        if (this.isEnabled)
            eliteMobData.add(this);
    }
}
