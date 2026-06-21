package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteZombie extends EliteMobProperties {

    public EliteZombie() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.ZOMBIE).getName();
        this.entityType = EntityType.ZOMBIE;
        this.defaultMaxHealth = 20;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.ZOMBIE).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.ZOMBIE).isEnabled();
        eliteMobData.add(this);
    }

}
