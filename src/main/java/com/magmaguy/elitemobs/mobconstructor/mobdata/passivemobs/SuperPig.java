package com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import org.bukkit.entity.EntityType;

public class SuperPig extends SuperMobProperties {

    public SuperPig() {

        this.name = ConfigValues.translationConfig.getString(TranslationConfig.NAME_PIG);

        this.entityType = EntityType.PIG;

        this.defaultMaxHealth = 10;

        this.isEnabled = ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.VALID_SUPERMOBS + getEntityType().toString()) &&
                ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.ALLOW_PASSIVE_SUPERMOBS);

        if (this.isEnabled) {
            superMobTypeList.add(this.entityType);
            superMobData.add(this);
        }

    }

}
