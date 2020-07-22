package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteWitherSkeleton extends EliteMobProperties {

    public EliteWitherSkeleton() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.WITHER_SKELETON).getName();
        this.entityType = EntityType.WITHER_SKELETON;
        this.defaultMaxHealth = 20;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.WITHER_SKELETON).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.WITHER_SKELETON).isEnabled();
        if (this.isEnabled)
            eliteMobData.add(this);
    }

}
