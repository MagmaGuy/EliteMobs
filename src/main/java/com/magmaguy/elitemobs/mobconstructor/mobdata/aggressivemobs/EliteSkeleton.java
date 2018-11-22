package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import com.magmaguy.elitemobs.mobpowers.majorpowers.SkeletonPillar;
import com.magmaguy.elitemobs.mobpowers.majorpowers.SkeletonTrackingArrow;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.HashSet;

public class EliteSkeleton extends EliteMobProperties {

    public EliteSkeleton() {

        this.name = ConfigValues.translationConfig.getString(TranslationConfig.NAME_SKELETON);

        this.entityType = EntityType.SKELETON;

        this.defaultMaxHealth = 20;

        SkeletonTrackingArrow skeletonTrackingArrow = new SkeletonTrackingArrow();
        SkeletonPillar skeletonPillar = new SkeletonPillar();
        this.validMajorPowers = new HashSet<>(Arrays.asList(skeletonPillar, skeletonTrackingArrow));

        this.validDefensivePowers.addAll(super.getAllDefensivePowers());
        this.validOffensivePowers.addAll(super.getAllOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getAllMiscellaneousPowers());

        this.isEnabled = ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.VALID_AGGRESSIVE_ELITEMOBS + getEntityType().toString()) &&
                ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.ALLOW_AGGRESSIVE_ELITEMOBS);

        if (this.isEnabled)
            eliteMobData.add(this);

    }

}
