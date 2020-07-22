package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.entity.EntityType;

public class EliteZoglin extends EliteMobProperties {
    public EliteZoglin() {
        if (VersionChecker.currentVersionIsUnder(16, 0)) {
            return;
        }
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.ZOGLIN).getName();
        this.entityType = EntityType.ZOGLIN;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.ZOGLIN).getBaseDamage();
        this.defaultMaxHealth = 40;
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.ZOGLIN).isEnabled();
        if (this.isEnabled)
            eliteMobData.add(this);
    }
}
