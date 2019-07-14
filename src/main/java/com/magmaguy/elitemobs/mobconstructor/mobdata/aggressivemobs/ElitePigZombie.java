package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class ElitePigZombie extends EliteMobProperties {

    public ElitePigZombie() {

        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.PIG_ZOMBIE).getName();

        this.entityType = EntityType.PIG_ZOMBIE;

        this.defaultMaxHealth = 20;

        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.PIG_ZOMBIE).isEnabled();

        if (this.isEnabled)
            eliteMobData.add(this);

    }

}
