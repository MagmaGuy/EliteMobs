package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteBreeze extends EliteMobProperties {

    public EliteBreeze() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.BREEZE).getName();
        this.entityType = EntityType.BREEZE;
        this.defaultMaxHealth = 30;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.BREEZE).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.BREEZE).isEnabled();
        eliteMobData.add(this);
    }
}
