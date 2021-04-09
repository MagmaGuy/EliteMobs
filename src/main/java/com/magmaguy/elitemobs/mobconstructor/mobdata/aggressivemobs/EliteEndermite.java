package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteEndermite extends EliteMobProperties {

    public EliteEndermite() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.ENDERMITE).getName();
        this.entityType = EntityType.ENDERMITE;
        this.defaultMaxHealth = 8;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.ENDERMITE).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.ENDERMITE).isEnabled();
            eliteMobData.add(this);
    }

}
