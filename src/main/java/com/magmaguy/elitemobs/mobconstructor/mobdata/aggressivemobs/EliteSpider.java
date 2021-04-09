package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteSpider extends EliteMobProperties {

    public EliteSpider() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.SPIDER).getName();
        this.entityType = EntityType.SPIDER;
        this.defaultMaxHealth = 16;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.SPIDER).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.SPIDER).isEnabled();
            eliteMobData.add(this);
    }

}
