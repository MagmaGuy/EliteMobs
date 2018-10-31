package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.mobconstructor.mobdata.PluginMobProperties;
import com.magmaguy.elitemobs.mobpowers.majorpowers.MajorPowers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

import static com.magmaguy.elitemobs.utils.VersionChecker.currentVersionIsUnder;

public abstract class EliteMobProperties extends PluginMobProperties {

    /*
    This class only defines sets of mostly config-based data to be used by the EliteMobEntity.java class
     */

    public static List<EliteMobProperties> eliteMobData = new ArrayList<>();
    public ArrayList<MajorPowers> validMajorPowers = new ArrayList<>();

    public List<MajorPowers> getValidMajorPowers() {
        return validMajorPowers;
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
            eliteMobData.add(eliteEndermite);
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

        Bukkit.getLogger().warning("[EliteMobs] Something is wrong with the Elite Mob data, notify the dev!");
        return null;

    }

    public static EliteMobProperties getPluginData(Entity entity) {
        return getPluginData(entity.getType());
    }

}
