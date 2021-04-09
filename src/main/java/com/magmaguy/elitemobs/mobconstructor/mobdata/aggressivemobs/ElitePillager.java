package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class ElitePillager extends EliteMobProperties {
    public ElitePillager() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.PILLAGER).getName();
        this.entityType = EntityType.PILLAGER;
        this.defaultMaxHealth = 24;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.PILLAGER).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.PILLAGER).isEnabled();
            eliteMobData.add(this);
    }
}
