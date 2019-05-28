package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import com.magmaguy.elitemobs.mobpowers.ElitePower;
import com.magmaguy.elitemobs.mobpowers.defensivepowers.Invisibility;
import org.bukkit.entity.EntityType;

public class EliteCreeper extends EliteMobProperties {

    public EliteCreeper() {

        this.name = ConfigValues.translationConfig.getString(TranslationConfig.NAME_CREEPER);

        this.entityType = EntityType.CREEPER;

        this.defaultMaxHealth = 20;

        this.validDefensivePowers.addAll(super.getAllDefensivePowers());
        this.validOffensivePowers.addAll(super.getAllOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getAllMiscellaneousPowers());

//        todo: add a way to configure powers per entity type
        ElitePower elitePower = null;
        for (ElitePower elitePower1 : this.validDefensivePowers)
            if (elitePower1 instanceof Invisibility)
                elitePower = elitePower1;

        if (elitePower != null)
            this.validDefensivePowers.remove(elitePower);

        isEnabled = ValidMobsConfig.getBoolean(ValidMobsConfig.VALID_AGGRESSIVE_ELITEMOBS + getEntityType().toString()) &&
                ValidMobsConfig.getBoolean(ValidMobsConfig.ALLOW_AGGRESSIVE_ELITEMOBS);

        if (this.isEnabled)
            eliteMobData.add(this);

    }

}
