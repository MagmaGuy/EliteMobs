package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.entity.EntityType;

public class ElitePiglinBrute extends EliteMobProperties {
    public ElitePiglinBrute() {
        if (VersionChecker.currentVersionIsUnder(16, 2)) {
            return;
        }
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.PIGLIN_BRUTE).getName();
        this.entityType = EntityType.PIGLIN_BRUTE;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.PIGLIN_BRUTE).getBaseDamage();
        this.defaultMaxHealth = 50;
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.PIGLIN_BRUTE).isEnabled();
        if (this.isEnabled)
            eliteMobData.add(this);
    }
}
