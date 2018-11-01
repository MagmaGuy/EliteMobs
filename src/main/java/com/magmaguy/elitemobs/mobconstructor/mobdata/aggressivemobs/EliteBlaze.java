package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import org.bukkit.entity.EntityType;

public class EliteBlaze extends EliteMobProperties {

    public EliteBlaze() {

        this.isEnabled = ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.VALID_AGGRESSIVE_ELITEMOBS.toLowerCase() + getEntityType().toString()) &&
                ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.ALLOW_AGGRESSIVE_ELITEMOBS);

        this.name = ConfigValues.translationConfig.getString(TranslationConfig.NAME_BLAZE);

        this.entityType = EntityType.BLAZE;

        this.defaultMaxHealth = 20;

        if (!isEnabled) return;

        this.validDefensivePowers.addAll(super.getValidDefensivePowers());
        this.validOffensivePowers.addAll(super.getValidOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getValidMiscellaneousPowers());

        eliteMobData.add(this);

    }

}
