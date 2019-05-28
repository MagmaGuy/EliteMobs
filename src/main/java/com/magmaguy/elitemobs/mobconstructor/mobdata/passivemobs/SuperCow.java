package com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import org.bukkit.entity.EntityType;

public class SuperCow extends SuperMobProperties {

    public SuperCow() {

        this.name = ConfigValues.translationConfig.getString(TranslationConfig.NAME_COW);

        this.entityType = EntityType.COW;

        this.defaultMaxHealth = 10;

        this.isEnabled = ValidMobsConfig.getBoolean(ValidMobsConfig.VALID_SUPERMOBS + getEntityType().toString()) &&
                ValidMobsConfig.getBoolean(ValidMobsConfig.ALLOW_PASSIVE_SUPERMOBS);

        if (this.isEnabled) {
            superMobTypeList.add(this.entityType);
            superMobData.add(this);
        }

    }

}
