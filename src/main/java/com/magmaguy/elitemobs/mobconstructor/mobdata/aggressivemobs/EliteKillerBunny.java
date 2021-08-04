package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteKillerBunny extends EliteMobProperties {
    public EliteKillerBunny() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.RABBIT).getName();
        this.entityType = EntityType.RABBIT;
        this.defaultMaxHealth = 3;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.RABBIT).getBaseDamage();
        isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.RABBIT).isEnabled();
        eliteMobData.add(this);
    }
}
