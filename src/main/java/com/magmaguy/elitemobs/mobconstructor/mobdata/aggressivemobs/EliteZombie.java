package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobPowersConfig;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieBloat;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieFriends;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieNecronomicon;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieParents;
import org.bukkit.entity.EntityType;

import java.util.HashSet;

public class EliteZombie extends EliteMobProperties {

    public EliteZombie() {

        this.name = ChatColorConverter.convert(MobPropertiesConfig.getMobProperties().get(EntityType.ZOMBIE).getName());

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

        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.ZOMBIE).isEnabled();

        if (this.isEnabled)
            eliteMobData.add(this);

    }

}
