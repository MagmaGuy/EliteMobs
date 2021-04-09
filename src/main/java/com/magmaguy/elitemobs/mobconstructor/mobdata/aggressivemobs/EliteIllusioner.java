package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteIllusioner extends EliteMobProperties {
    public EliteIllusioner() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.ILLUSIONER).getName();
        this.entityType = EntityType.ILLUSIONER;
        this.defaultMaxHealth = 32;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.ILLUSIONER).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.ILLUSIONER).isEnabled();
            eliteMobData.add(this);
    }
}
