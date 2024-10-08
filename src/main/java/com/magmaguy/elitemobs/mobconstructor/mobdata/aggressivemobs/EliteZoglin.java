package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteZoglin extends EliteMobProperties {
    public EliteZoglin() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.ZOGLIN).getName();
        this.entityType = EntityType.ZOGLIN;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.ZOGLIN).getBaseDamage();
        this.defaultMaxHealth = 40;
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.ZOGLIN).isEnabled();
        eliteMobData.add(this);
    }
}
