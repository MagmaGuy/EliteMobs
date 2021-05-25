package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteEndCrystal extends EliteMobProperties {
    public EliteEndCrystal() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.ENDER_CRYSTAL).getName();
        this.entityType = EntityType.ENDER_CRYSTAL;
        this.defaultMaxHealth = 1;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.ENDER_CRYSTAL).getBaseDamage();
        isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.ENDER_CRYSTAL).isEnabled();
        eliteMobData.add(this);
    }
}
