package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteHusk extends EliteMobProperties {

    public EliteHusk() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.HUSK).getName();
        this.entityType = EntityType.HUSK;
        this.defaultMaxHealth = 20;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.HUSK).getBaseDamage();
        isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.HUSK).isEnabled();
            eliteMobData.add(this);
    }

}
