package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.entity.EntityType;

public class ElitePiglin extends EliteMobProperties {
    public ElitePiglin() {
        if (VersionChecker.serverVersionOlderThan(16, 0)) {
            return;
        }
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.PIGLIN).getName();
        this.entityType = EntityType.PIGLIN;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.PIGLIN).getBaseDamage();
        this.defaultMaxHealth = 16;
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.PIGLIN).isEnabled();
            eliteMobData.add(this);
    }
}
