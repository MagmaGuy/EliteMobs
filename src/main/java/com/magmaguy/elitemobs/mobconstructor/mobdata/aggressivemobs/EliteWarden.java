package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.entity.EntityType;

public class EliteWarden extends EliteMobProperties {
    public EliteWarden(){
        if (VersionChecker.serverVersionOlderThan(19, 0)) {
            return;
        }
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.WARDEN).getName();
        this.entityType = EntityType.WARDEN;
        this.defaultMaxHealth = 500;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.WARDEN).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.WARDEN).isEnabled();
        eliteMobData.add(this);
    }
}
