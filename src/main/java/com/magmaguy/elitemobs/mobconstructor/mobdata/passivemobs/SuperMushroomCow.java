package com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import org.bukkit.entity.EntityType;

public class SuperMushroomCow extends SuperMobProperties {

    public SuperMushroomCow() {

        this.isEnabled = ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.VALID_SUPERMOBS.toLowerCase() + getEntityType().toString()) &&
                ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.ALLOW_PASSIVE_SUPERMOBS);

        this.name = ConfigValues.translationConfig.getString(TranslationConfig.NAME_MUSHROOM_COW);

        this.entityType = EntityType.MUSHROOM_COW;

        this.defaultMaxHealth = 10;

    }

}
