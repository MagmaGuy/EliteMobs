package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteBee extends EliteMobProperties {

    public EliteBee() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.BEE).getName();
        this.entityType = EntityType.BEE;
        this.defaultMaxHealth = 10;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.BEE).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.BEE).isEnabled();
        eliteMobData.add(this);
    }
}
