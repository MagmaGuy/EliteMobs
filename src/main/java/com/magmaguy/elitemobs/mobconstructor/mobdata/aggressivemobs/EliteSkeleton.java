package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteSkeleton extends EliteMobProperties {

    public EliteSkeleton() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.SKELETON).getName();
        this.entityType = EntityType.SKELETON;
        this.defaultMaxHealth = 20;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.SKELETON).getBaseDamage();
        super.addMajorPower("skeleton_pillar.yml");
        super.addMajorPower("skeleton_tracking_arrow.yml");
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.SKELETON).isEnabled();
            eliteMobData.add(this);
    }

}
