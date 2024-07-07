package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteWither extends EliteMobProperties {

    public EliteWither() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.WITHER).getName();
        this.entityType = EntityType.WITHER;
        this.defaultMaxHealth = 300;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.WITHER).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.WITHER).isEnabled();
        eliteMobData.add(this);
    }

}
