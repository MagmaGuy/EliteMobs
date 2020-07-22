package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.powers.defensivepowers.Invisibility;
import org.bukkit.entity.EntityType;

public class EliteCreeper extends EliteMobProperties {

    public EliteCreeper() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.CREEPER).getName();
        this.entityType = EntityType.CREEPER;
        this.defaultMaxHealth = 20;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.CREEPER).getBaseDamage();
        super.removeDefensivePower(new Invisibility());
        isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.CREEPER).isEnabled();
        if (this.isEnabled)
            eliteMobData.add(this);
    }

}
