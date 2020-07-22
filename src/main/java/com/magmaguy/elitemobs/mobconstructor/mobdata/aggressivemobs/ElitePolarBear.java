package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class ElitePolarBear extends EliteMobProperties {

    public ElitePolarBear() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.POLAR_BEAR).getName();
        this.entityType = EntityType.POLAR_BEAR;
        this.defaultMaxHealth = 30;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.POLAR_BEAR).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.POLAR_BEAR).isEnabled();
        if (this.isEnabled)
            eliteMobData.add(this);
    }

}
