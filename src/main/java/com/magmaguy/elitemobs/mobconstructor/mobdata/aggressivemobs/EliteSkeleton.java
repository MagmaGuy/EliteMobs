package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import com.magmaguy.elitemobs.mobpowers.majorpowers.SkeletonPillar;
import com.magmaguy.elitemobs.mobpowers.majorpowers.SkeletonTrackingArrow;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;

public class EliteSkeleton extends EliteMobProperties {

    public EliteSkeleton() {

        this.isEnabled = ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.VALID_AGGRESSIVE_ELITEMOBS.toLowerCase() + getEntityType().toString()) &&
                ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.ALLOW_AGGRESSIVE_ELITEMOBS);

        this.name = ConfigValues.translationConfig.getString(TranslationConfig.NAME_SKELETON);

        this.entityType = EntityType.SKELETON;

        this.defaultMaxHealth = 20;

        SkeletonTrackingArrow skeletonTrackingArrow = new SkeletonTrackingArrow();
        SkeletonPillar skeletonPillar = new SkeletonPillar();
        this.validMajorPowers = new ArrayList<>(Arrays.asList(skeletonPillar, skeletonTrackingArrow));

        if (!isEnabled) return;

        eliteMobData.add(this);

    }

}
