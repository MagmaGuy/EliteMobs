package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.mobdata.PluginMobProperties;
import com.magmaguy.elitemobs.powers.ElitePower;
import com.magmaguy.elitemobs.powers.MajorPower;
import com.magmaguy.elitemobs.powers.MinorPower;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.HashSet;
import java.util.Iterator;

public abstract class EliteMobProperties extends PluginMobProperties {

    /*
    This class only defines sets of mostly config-based data to be used by the EliteMobEntity.java class
     */

    public static HashSet<EliteMobProperties> eliteMobData = new HashSet<>();
    private final HashSet<MajorPower> validMajorPowers = new HashSet<>();
    private final HashSet<MinorPower> validDefensivePowers = (HashSet<MinorPower>) ElitePower.getDefensivePowers().clone();
    private final HashSet<MinorPower> validOffensivePowers = (HashSet<MinorPower>) ElitePower.getOffensivePowers().clone();
    private final HashSet<MinorPower> validMiscellaneousPowers = (HashSet<MinorPower>) ElitePower.getMiscellaneousPowers().clone();

    public void addMajorPower(String powerName) {
        if (PowersConfig.getPower(powerName).isEnabled())
            validMajorPowers.add((MajorPower) ElitePower.getElitePower(powerName));
    }

    public void removeOffensivePower(MinorPower minorPower) {
        for (Iterator<MinorPower> iterator = validOffensivePowers.iterator(); iterator.hasNext(); ) {
            MinorPower minorPower1 = iterator.next();
            if (minorPower1.getFileName().equalsIgnoreCase(minorPower.getFileName())) {
                iterator.remove();
                return;
            }
        }
    }

    public void removeDefensivePower(MinorPower minorPower) {
        for (Iterator<MinorPower> iterator = validDefensivePowers.iterator(); iterator.hasNext(); ) {
            MinorPower minorPower1 = iterator.next();
            if (minorPower1.getFileName().equalsIgnoreCase(minorPower.getFileName()))
                iterator.remove();
        }
    }

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

    public static void initializeEliteMobValues() {
        new EliteBlaze();
        new EliteCaveSpider();
        new EliteCreeper();
        new EliteDrowned();
        new EliteElderGuardian();
        new EliteGuardian();
        new EliteEnderman();
        new EliteIronGolem();
        new ElitePhantom();
        new EliteZombifiedPiglin();
        new ElitePillager();
        new EliteSilverfish();
        new EliteSkeleton();
        new EliteSpider();
        new EliteWitch();
        new EliteWitherSkeleton();
        new EliteZombie();
        new EliteEndermite();
        new EliteEvoker();
        new EliteStray();
        new EliteHusk();
        new EliteIllusioner();
        new EliteVex();
        new EliteVindicator();
        new ElitePolarBear();
        new EliteRavager();
        new EliteGhast();
        new EliteZoglin();
        new ElitePiglin();
        new EliteHoglin();
        new ElitePiglinBrute();
        new EliteBee();
        new EliteWolf();
    }

    public static boolean isValidEliteMobType(Entity entity) {
        if (entity instanceof LivingEntity)
            return isValidEliteMobType(entity.getType());
        return false;
    }

    public static boolean isValidEliteMobType(EntityType entityType) {
        for (EliteMobProperties eliteMobProperties : eliteMobData)
            if (eliteMobProperties.getEntityType().equals(entityType))
                if (eliteMobProperties.isEnabled)
                    return true;
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
