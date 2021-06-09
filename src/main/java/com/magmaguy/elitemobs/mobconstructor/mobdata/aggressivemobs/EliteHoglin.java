package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.entity.EntityType;

public class EliteHoglin extends EliteMobProperties {
    public EliteHoglin() {
        if (VersionChecker.serverVersionOlderThan(16, 0)) {
            return;
        }
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.HOGLIN).getName();
        this.entityType = EntityType.HOGLIN;
        this.defaultMaxHealth = 40;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.HOGLIN).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.HOGLIN).isEnabled();
            eliteMobData.add(this);
    }
}
