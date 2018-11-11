package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.mobconstructor.mobdata.PluginMobProperties;
import com.magmaguy.elitemobs.mobpowers.defensivepowers.*;
import com.magmaguy.elitemobs.mobpowers.majorpowers.MajorPower;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPower;
import com.magmaguy.elitemobs.mobpowers.miscellaneouspowers.BonusLoot;
import com.magmaguy.elitemobs.mobpowers.miscellaneouspowers.MovementSpeed;
import com.magmaguy.elitemobs.mobpowers.miscellaneouspowers.Taunt;
import com.magmaguy.elitemobs.mobpowers.offensivepowers.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.Arrays;
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
        return new HashSet<>(Arrays.asList(
                new Invisibility(),
                new InvulnerabilityArrow(),
                new InvulnerabilityFallDamage(),
                new InvulnerabilityFire(),
                new InvulnerabilityKnockback()
        ));
    }

    public HashSet<MinorPower> getAllOffensivePowers() {
        return new HashSet<>(Arrays.asList(
                new AttackArrow(),
                new AttackBlinding(),
                new AttackConfusing(),
                new AttackFire(),
                new AttackFireball(),
                new AttackFreeze(),
                new AttackGravity(),
                new AttackPoison(),
                new AttackPush(),
                new AttackWeakness(),
                new AttackWeb(),
                new AttackWither()
        ));
    }

    public HashSet<MinorPower> getAllMiscellaneousPowers() {
        return new HashSet<>(Arrays.asList(
                new BonusLoot(),
                new MovementSpeed(),
                new Taunt()
        ));
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

}
