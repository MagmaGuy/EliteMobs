package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import org.bukkit.entity.EntityType;

public class EliteGoat extends EliteMobProperties {
    public EliteGoat() {
        if (VersionChecker.serverVersionOlderThan(17, 0)) {
            return;
        }
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.GOAT).getName();
        this.entityType = EntityType.GOAT;
        this.defaultMaxHealth = 10;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.GOAT).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.GOAT).isEnabled();
        eliteMobData.add(this);
    }
}
