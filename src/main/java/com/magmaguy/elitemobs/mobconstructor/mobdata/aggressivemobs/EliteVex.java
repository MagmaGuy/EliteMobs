package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import org.bukkit.entity.EntityType;

public class EliteVex extends EliteMobProperties {

    public EliteVex() {

        this.isEnabled = ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.VALID_AGGRESSIVE_ELITEMOBS.toLowerCase() + getEntityType().toString()) &&
                ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.ALLOW_AGGRESSIVE_ELITEMOBS);

        this.name = ConfigValues.translationConfig.getString(TranslationConfig.NAME_VEX);

        this.entityType = EntityType.VEX;

        this.defaultMaxHealth = 14;

        if (!isEnabled) return;

        this.validDefensivePowers.addAll(super.getValidDefensivePowers());
        this.validOffensivePowers.addAll(super.getValidOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getValidMiscellaneousPowers());

        eliteMobData.add(this);

    }

}
