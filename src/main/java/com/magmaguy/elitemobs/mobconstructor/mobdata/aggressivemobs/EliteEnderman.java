package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteEnderman extends EliteMobProperties {

    public EliteEnderman() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.ENDERMAN).getName();
        this.entityType = EntityType.ENDERMAN;
        this.defaultMaxHealth = 40;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.ENDERMAN).getBaseDamage();
        isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.ENDERMAN).isEnabled();
        eliteMobData.add(this);
    }

}
