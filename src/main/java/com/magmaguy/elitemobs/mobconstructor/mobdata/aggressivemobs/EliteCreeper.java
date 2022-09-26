package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteCreeper extends EliteMobProperties {

    public EliteCreeper() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.CREEPER).getName();
        this.entityType = EntityType.CREEPER;
        this.defaultMaxHealth = 20;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.CREEPER).getBaseDamage();
        super.removeDefensivePower("invisibility.yml");
        isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.CREEPER).isEnabled();
        eliteMobData.add(this);
    }

}
