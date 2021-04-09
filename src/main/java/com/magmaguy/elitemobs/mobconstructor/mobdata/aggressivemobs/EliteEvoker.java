package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteEvoker extends EliteMobProperties {
    public EliteEvoker() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.EVOKER).getName();
        this.entityType = EntityType.EVOKER;
        this.defaultMaxHealth = 24;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.EVOKER).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.EVOKER).isEnabled();
            eliteMobData.add(this);
    }
}
