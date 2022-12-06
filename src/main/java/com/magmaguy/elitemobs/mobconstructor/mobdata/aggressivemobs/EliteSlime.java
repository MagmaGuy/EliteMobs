package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteSlime extends EliteMobProperties {
    public EliteSlime() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.SLIME).getName();
        this.entityType = EntityType.SLIME;
        this.defaultMaxHealth = 16;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.SLIME).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.SLIME).isEnabled();
        eliteMobData.add(this);
    }
}
