package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteGoat extends EliteMobProperties {
    public EliteGoat() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.GOAT).getName();
        this.entityType = EntityType.GOAT;
        this.defaultMaxHealth = 10;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.GOAT).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.GOAT).isEnabled();
        eliteMobData.add(this);
    }
}
