package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteShulker extends EliteMobProperties{
    public EliteShulker(){
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.SHULKER).getName();
        this.entityType = EntityType.SHULKER;
        this.defaultMaxHealth = 30;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.SHULKER).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.SHULKER).isEnabled();
        eliteMobData.add(this);
    }
}
