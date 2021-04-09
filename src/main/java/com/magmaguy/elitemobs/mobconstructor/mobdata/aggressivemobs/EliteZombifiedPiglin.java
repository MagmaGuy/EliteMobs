package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.entity.EntityType;

public class EliteZombifiedPiglin extends EliteMobProperties {
    public EliteZombifiedPiglin() {
        if (VersionChecker.currentVersionIsUnder(16, 0)) {
            return;
        }
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.ZOMBIFIED_PIGLIN).getName();
        this.entityType = EntityType.ZOMBIFIED_PIGLIN;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.ZOMBIFIED_PIGLIN).getBaseDamage();
        this.defaultMaxHealth = 20;
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.ZOMBIFIED_PIGLIN).isEnabled();
            eliteMobData.add(this);
    }
}
