package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import com.magmaguy.elitemobs.mobpowers.majorpowers.*;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.HashSet;

public class EliteZombie extends EliteMobProperties {

    public EliteZombie() {

        this.name = ConfigValues.translationConfig.getString(TranslationConfig.NAME_ZOMBIE);

        this.entityType = EntityType.ZOMBIE;

        this.defaultMaxHealth = 20;

        ZombieBloat zombieBloat = new ZombieBloat();
        ZombieFriends zombieFriends = new ZombieFriends();
        ZombieNecronomicon zombieNecronomicon = new ZombieNecronomicon();
        ZombieParents zombieParents = new ZombieParents();
        ZombieTeamRocket zombieTeamRocket = new ZombieTeamRocket();
        this.validMajorPowers = new HashSet<>(Arrays.asList(zombieBloat, zombieFriends, zombieNecronomicon, zombieParents, zombieTeamRocket));

        this.validDefensivePowers.addAll(super.getAllDefensivePowers());
        this.validOffensivePowers.addAll(super.getAllOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getAllMiscellaneousPowers());

        this.isEnabled = ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.VALID_AGGRESSIVE_ELITEMOBS + getEntityType().toString()) &&
                ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.ALLOW_AGGRESSIVE_ELITEMOBS);

        if (this.isEnabled)
            eliteMobData.add(this);

    }

}
