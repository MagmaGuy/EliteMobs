package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteCaveSpider extends EliteMobProperties {

    public EliteCaveSpider() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.CAVE_SPIDER).getName();
        this.entityType = EntityType.CAVE_SPIDER;
        this.defaultMaxHealth = 12;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.CAVE_SPIDER).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.CAVE_SPIDER).isEnabled();
        if (this.isEnabled)
            eliteMobData.add(this);
    }

}
