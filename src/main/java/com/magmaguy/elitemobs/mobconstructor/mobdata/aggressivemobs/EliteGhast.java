package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteGhast extends EliteMobProperties {
    public EliteGhast() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.GHAST).getName();
        this.entityType = EntityType.GHAST;
        this.defaultMaxHealth = 10;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.GHAST).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.GHAST).isEnabled();
        if (this.isEnabled)
            eliteMobData.add(this);
    }
}
