package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class ElitePhantom extends EliteMobProperties {

    public ElitePhantom() {

        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.PHANTOM).getName();

        this.entityType = EntityType.PHANTOM;

        this.defaultMaxHealth = 20;

        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.PHANTOM).isEnabled();

        if (this.isEnabled)
            eliteMobData.add(this);

    }

}
