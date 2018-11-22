package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import org.bukkit.entity.EntityType;

public class EliteCaveSpider extends EliteMobProperties {

    public EliteCaveSpider() {

        this.name = ConfigValues.translationConfig.getString(TranslationConfig.NAME_CAVE_SPIDER);

        this.entityType = EntityType.CAVE_SPIDER;

        this.defaultMaxHealth = 12;

        this.validDefensivePowers.addAll(super.getAllDefensivePowers());
        this.validOffensivePowers.addAll(super.getAllOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getAllMiscellaneousPowers());

        this.isEnabled = ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.VALID_AGGRESSIVE_ELITEMOBS + getEntityType().toString()) &&
                ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.ALLOW_AGGRESSIVE_ELITEMOBS);

        if (this.isEnabled)
            eliteMobData.add(this);

    }

}
