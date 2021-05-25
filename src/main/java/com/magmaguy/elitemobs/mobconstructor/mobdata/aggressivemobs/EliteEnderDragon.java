package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteEnderDragon extends EliteMobProperties {
    public EliteEnderDragon(){
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.ENDER_DRAGON).getName();
        this.entityType = EntityType.ENDER_DRAGON;
        this.defaultMaxHealth = 200;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.ENDER_DRAGON).getBaseDamage();
        isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.ENDER_DRAGON).isEnabled();
        eliteMobData.add(this);
    }
}
