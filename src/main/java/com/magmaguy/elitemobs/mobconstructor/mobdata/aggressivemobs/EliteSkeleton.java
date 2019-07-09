package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobPowersConfig;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.powers.majorpowers.skeleton.SkeletonPillar;
import com.magmaguy.elitemobs.powers.majorpowers.skeleton.SkeletonTrackingArrow;
import org.bukkit.entity.EntityType;

import java.util.HashSet;

public class EliteSkeleton extends EliteMobProperties {

    public EliteSkeleton() {

        this.name = ChatColorConverter.convert(MobPropertiesConfig.getMobProperties().get(EntityType.SKELETON).getName());

        this.entityType = EntityType.SKELETON;

        this.defaultMaxHealth = 20;

        this.validMajorPowers = new HashSet<>();
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.SKELETON_PILLAR))
            this.validMajorPowers.add(new SkeletonPillar());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.SKELETON_TRACKING_ARROW))
            this.validMajorPowers.add(new SkeletonTrackingArrow());

        this.validDefensivePowers.addAll(super.getAllDefensivePowers());
        this.validOffensivePowers.addAll(super.getAllOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getAllMiscellaneousPowers());

        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.SKELETON).isEnabled();

        if (this.isEnabled)
            eliteMobData.add(this);

    }

}
