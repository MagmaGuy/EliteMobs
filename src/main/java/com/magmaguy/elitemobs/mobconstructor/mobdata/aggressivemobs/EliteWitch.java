package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteWitch extends EliteMobProperties {

    public EliteWitch() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.WITCH).getName();
        this.entityType = EntityType.WITCH;
        this.defaultMaxHealth = 26;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.WITCH).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.WITCH).isEnabled();
            eliteMobData.add(this);
    }

}
