package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteStray extends EliteMobProperties {

    public EliteStray() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.STRAY).getName();
        this.entityType = EntityType.STRAY;
        this.defaultMaxHealth = 20;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.STRAY).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.STRAY).isEnabled();
        if (this.isEnabled)
            eliteMobData.add(this);
    }

}
