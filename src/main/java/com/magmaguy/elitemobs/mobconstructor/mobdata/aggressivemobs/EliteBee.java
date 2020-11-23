package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.entity.EntityType;

public class EliteBee extends EliteMobProperties {

    public EliteBee() {
        if (VersionChecker.currentVersionIsUnder(15, 0)) {
            return;
        }
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.BEE).getName();
        this.entityType = EntityType.BEE;
        this.defaultMaxHealth = 10;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.BEE).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.BEE).isEnabled();
        if (this.isEnabled)
            eliteMobData.add(this);
    }
}
