package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteVex extends EliteMobProperties {

    public EliteVex() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.VEX).getName();
        this.entityType = EntityType.VEX;
        this.defaultMaxHealth = 14;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.VEX).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.VEX).isEnabled();
        super.removeDefensivePower("ground_pound.yml");
        eliteMobData.add(this);
    }

}
