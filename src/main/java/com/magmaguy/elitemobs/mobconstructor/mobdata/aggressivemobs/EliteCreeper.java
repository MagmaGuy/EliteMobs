package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import com.magmaguy.elitemobs.mobpowers.defensivepowers.Invisibility;
import org.bukkit.entity.EntityType;

public class EliteCreeper extends EliteMobProperties {

    public EliteCreeper() {

        isEnabled = ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.VALID_AGGRESSIVE_ELITEMOBS.toLowerCase() + getEntityType().toString()) &&
                ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.ALLOW_AGGRESSIVE_ELITEMOBS);

        this.name = ConfigValues.translationConfig.getString(TranslationConfig.NAME_CREEPER);

        this.entityType = EntityType.CREEPER;

        this.defaultMaxHealth = 20;

        if (!isEnabled) return;

        this.validDefensivePowers.addAll(super.getValidDefensivePowers());
        this.validOffensivePowers.addAll(super.getValidOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getValidMiscellaneousPowers());

//        todo: add a way to configure powers per entity type
        this.validMiscellaneousPowers.remove(new Invisibility());

        eliteMobData.add(this);

    }

}
