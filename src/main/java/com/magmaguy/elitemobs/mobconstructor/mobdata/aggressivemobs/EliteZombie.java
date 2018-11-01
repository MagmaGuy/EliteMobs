package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import com.magmaguy.elitemobs.mobpowers.majorpowers.*;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;

public class EliteZombie extends EliteMobProperties {

    public EliteZombie() {

        this.isEnabled = ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.VALID_AGGRESSIVE_ELITEMOBS.toLowerCase() + getEntityType().toString()) &&
                ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.ALLOW_AGGRESSIVE_ELITEMOBS);

        this.name = ConfigValues.translationConfig.getString(TranslationConfig.NAME_ZOMBIE);

        this.entityType = EntityType.ZOMBIE;

        this.defaultMaxHealth = 20;

        ZombieBloat zombieBloat = new ZombieBloat();
        ZombieFriends zombieFriends = new ZombieFriends();
        ZombieNecronomicon zombieNecronomicon = new ZombieNecronomicon();
        ZombieParents zombieParents = new ZombieParents();
        ZombieTeamRocket zombieTeamRocket = new ZombieTeamRocket();
        this.validMajorPowers = new ArrayList<>(Arrays.asList(zombieBloat, zombieFriends, zombieNecronomicon, zombieParents, zombieTeamRocket));

        if (!isEnabled) return;

        this.validDefensivePowers.addAll(super.getValidDefensivePowers());
        this.validOffensivePowers.addAll(super.getValidOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getValidMiscellaneousPowers());

        eliteMobData.add(this);

    }

}
