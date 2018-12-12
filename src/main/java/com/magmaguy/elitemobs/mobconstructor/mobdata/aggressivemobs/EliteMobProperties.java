package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobPowersConfig;
import com.magmaguy.elitemobs.mobconstructor.mobdata.PluginMobProperties;
import com.magmaguy.elitemobs.mobpowers.defensivepowers.*;
import com.magmaguy.elitemobs.mobpowers.majorpowers.MajorPower;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPower;
import com.magmaguy.elitemobs.mobpowers.miscellaneouspowers.*;
import com.magmaguy.elitemobs.mobpowers.offensivepowers.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.HashSet;

import static com.magmaguy.elitemobs.utils.VersionChecker.currentVersionIsUnder;

public abstract class EliteMobProperties extends PluginMobProperties {

    /*
    This class only defines sets of mostly config-based data to be used by the EliteMobEntity.java class
     */

    public static HashSet<EliteMobProperties> eliteMobData = new HashSet<>();
    public HashSet<MajorPower> validMajorPowers = new HashSet<>();
    public HashSet<MinorPower> validDefensivePowers = new HashSet<>();
    public HashSet<MinorPower> validOffensivePowers = new HashSet<>();
    public HashSet<MinorPower> validMiscellaneousPowers = new HashSet<>();

    public HashSet<MajorPower> getValidMajorPowers() {
        return validMajorPowers;
    }

    public HashSet<MinorPower> getValidDefensivePowers() {
        return validDefensivePowers;
    }

    public HashSet<MinorPower> getValidOffensivePowers() {
        return validOffensivePowers;
    }

    public HashSet<MinorPower> getValidMiscellaneousPowers() {
        return validMiscellaneousPowers;
    }

    public HashSet<MinorPower> getAllDefensivePowers() {
        HashSet<MinorPower> defensivePowers = new HashSet<>();

        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.INVISIBILITY))
            defensivePowers.add(new Invisibility());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.INVULNERABILITY_ARROW))
            defensivePowers.add(new InvulnerabilityArrow());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.INVULNERABILITY_FALL_DAMAGE))
            defensivePowers.add(new InvulnerabilityFallDamage());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.INVULNERABILITY_FIRE))
            defensivePowers.add(new InvulnerabilityFire());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.INVULNERABILITY_KNOCKBACK))
            defensivePowers.add(new InvulnerabilityKnockback());

        return defensivePowers;
    }

    public HashSet<MinorPower> getAllOffensivePowers() {
        HashSet<MinorPower> offensivePowers = new HashSet<>();

        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.ATTACK_ARROW))
            offensivePowers.add(new AttackArrow());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.ATTACK_BLINDING))
            offensivePowers.add(new AttackBlinding());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.ATTACK_CONFUSING))
            offensivePowers.add(new AttackConfusing());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.ATTACK_FIRE))
            offensivePowers.add(new AttackFire());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.ATTACK_FIREBALL))
            offensivePowers.add(new AttackFireball());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.ATTACK_FREEZE))
            offensivePowers.add(new AttackFreeze());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.ATTACK_GRAVITY))
            offensivePowers.add(new AttackGravity());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.ATTACK_POISON))
            offensivePowers.add(new AttackPoison());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.ATTACK_PUSH))
            offensivePowers.add(new AttackPush());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.ATTACK_WEAKNESS))
            offensivePowers.add(new AttackWeakness());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.ATTACK_WEAKNESS))
            offensivePowers.add(new AttackWeb());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.ATTACK_WITHER))
            offensivePowers.add(new AttackWither());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.ATTACK_VACUUM))
            offensivePowers.add(new AttackVacuum());

        return offensivePowers;
    }

    public HashSet<MinorPower> getAllMiscellaneousPowers() {
        HashSet<MinorPower> miscellaneousPowers = new HashSet<>();

        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.BONUS_LOOT))
            miscellaneousPowers.add(new BonusLoot());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.MOVEMENT_SPEED))
            miscellaneousPowers.add(new MovementSpeed());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.TAUNT))
            miscellaneousPowers.add(new Taunt());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.CORPSE))
            miscellaneousPowers.add(new Corpse());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.MOON_WALK))
            miscellaneousPowers.add(new MoonWalk());
        if (ConfigValues.mobPowerConfig.getBoolean(MobPowersConfig.IMPLOSION))
            miscellaneousPowers.add(new Implosion());

        return miscellaneousPowers;
    }

    public static void initializeEliteMobValues() {
        EliteBlaze eliteBlaze = new EliteBlaze();
        EliteCaveSpider eliteCaveSpider = new EliteCaveSpider();
        EliteCreeper eliteCreeper = new EliteCreeper();
        EliteEnderman eliteEnderman = new EliteEnderman();
        EliteIronGolem eliteIronGolem = new EliteIronGolem();
        ElitePigZombie elitePigZombie = new ElitePigZombie();
        EliteSilverfish eliteSilverfish = new EliteSilverfish();
        EliteSkeleton eliteSkeleton = new EliteSkeleton();
        EliteSpider eliteSpider = new EliteSpider();
        EliteWitch eliteWitch = new EliteWitch();
        EliteWitherSkeleton eliteWitherSkeleton = new EliteWitherSkeleton();
        EliteZombie eliteZombie = new EliteZombie();
        /*
        Post-1.8
         */
        if (!currentVersionIsUnder(1, 8)) {
            EliteEndermite eliteEndermite = new EliteEndermite();
        }
        /*
        Post-1.11
         */
        if (!currentVersionIsUnder(1, 11)) {
            EliteStray eliteStray = new EliteStray();
            EliteHusk eliteHusk = new EliteHusk();
            EliteVex eliteVex = new EliteVex();
            EliteVindicator eliteVindicator = new EliteVindicator();
            ElitePolarBear elitePolarBear = new ElitePolarBear();
        }
    }

    public static boolean isValidEliteMobType(EntityType entityType) {

        for (EliteMobProperties eliteMobProperties : eliteMobData)
            if (eliteMobProperties.getEntityType().equals(entityType))
                return true;

        return false;

    }

    public static boolean isValidEliteMobType(Entity entity) {
        if (entity instanceof LivingEntity)
            return isValidEliteMobType(entity.getType());
        return false;
    }

    public static EliteMobProperties getPluginData(EntityType entityType) {

        for (EliteMobProperties eliteMobProperties : eliteMobData)
            if (eliteMobProperties.getEntityType().equals(entityType))
                return eliteMobProperties;

        return null;

    }

    public static EliteMobProperties getPluginData(Entity entity) {
        return getPluginData(entity.getType());
    }

    public static HashSet<EntityType> getValidMobTypes() {
        HashSet<EntityType> livingEntities = new HashSet<>();
        for (EliteMobProperties eliteMobProperties : eliteMobData)
            livingEntities.add(eliteMobProperties.getEntityType());
        return livingEntities;
    }

}
