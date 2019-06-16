package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobPowersConfig;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import com.magmaguy.elitemobs.mobpowers.majorpowers.zombie.ZombieBloat;
import com.magmaguy.elitemobs.mobpowers.majorpowers.zombie.ZombieFriends;
import com.magmaguy.elitemobs.mobpowers.majorpowers.zombie.ZombieNecronomicon;
import com.magmaguy.elitemobs.mobpowers.majorpowers.zombie.ZombieParents;
import org.bukkit.entity.EntityType;

import java.util.HashSet;

public class EliteZombie extends EliteMobProperties {

    public EliteZombie() {

        this.name = ConfigValues.translationConfig.getString(TranslationConfig.NAME_ZOMBIE);

        this.entityType = EntityType.ZOMBIE;

        this.defaultMaxHealth = 20;

        this.validMajorPowers = new HashSet<>();
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.ZOMBIE_BLOAT))
            this.validMajorPowers.add(new ZombieBloat());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.ZOMBIE_FRIENDS))
            this.validMajorPowers.add(new ZombieFriends());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.ZOMBIE_NECRONOMICON))
            this.validMajorPowers.add(new ZombieNecronomicon());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.ZOMBIE_PARENTS))
            this.validMajorPowers.add(new ZombieParents());

        this.validDefensivePowers.addAll(super.getAllDefensivePowers());
        this.validOffensivePowers.addAll(super.getAllOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getAllMiscellaneousPowers());

        this.isEnabled = ValidMobsConfig.getBoolean(ValidMobsConfig.VALID_AGGRESSIVE_ELITEMOBS + getEntityType().toString()) &&
                ValidMobsConfig.getBoolean(ValidMobsConfig.ALLOW_AGGRESSIVE_ELITEMOBS);

        if (this.isEnabled)
            eliteMobData.add(this);

    }

}
