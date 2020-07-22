package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteIronGolem extends EliteMobProperties {

    public EliteIronGolem() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.IRON_GOLEM).getName();
        this.entityType = EntityType.IRON_GOLEM;
        this.defaultMaxHealth = 100;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.IRON_GOLEM).getBaseDamage();
        isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.IRON_GOLEM).isEnabled();
        if (this.isEnabled)
            eliteMobData.add(this);
    }

}
