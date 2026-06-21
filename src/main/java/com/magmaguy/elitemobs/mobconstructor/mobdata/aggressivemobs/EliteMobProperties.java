package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.EliteMobPowersConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.PluginMobProperties;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.HashSet;

public abstract class EliteMobProperties extends PluginMobProperties {

    /*
    This class only defines sets of mostly config-based data to be used by the EliteMobEntity.java class
     */

    public static HashSet<EliteMobProperties> eliteMobData = new HashSet<>();
    @Getter
    private final HashSet<PowersConfigFields> validMajorPowers = new HashSet<>();
    @Getter
    private final HashSet<PowersConfigFields> validDefensivePowers = EliteMobPowersConfig.getDefensivePowerFields();
    @Getter
    private final HashSet<PowersConfigFields> validOffensivePowers = EliteMobPowersConfig.getOffensivePowerFields();
    @Getter
    private final HashSet<PowersConfigFields> validMiscellaneousPowers = EliteMobPowersConfig.getMiscellaneousPowerFields();

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
        new EliteEnderDragon();
        new EliteShulker();
        new EliteKillerBunny();
        new EliteGoat();
        new EliteLlama();
        new EliteWarden();
        new EliteSlime();
        new EliteMagmaCube();
        new EliteBogged();
        new EliteBreeze();
        new EliteWither();

        eliteMobData.forEach(EliteMobProperties::applyConfiguredPowerSettings);
    }

    public static void shutdown() {
        eliteMobData.clear();
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

    public static double getBaselineDamage(EntityType entityType, EliteEntity eliteEntity) {
        if (eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.isNormalizedCombat())
            return MobCombatSettingsConfig.getNormalizedBaselineDamage();
        return getPluginData(entityType).baseDamage;
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

    public void addMajorPower(String powerName) {
        PowersConfigFields powersConfigFields = PowersConfig.getPower(powerName);
        if (powersConfigFields != null && powersConfigFields.isEnabled())
            validMajorPowers.add(powersConfigFields);
    }

    private void applyConfiguredPowerSettings() {
        validMajorPowers.addAll(EliteMobPowersConfig.getMajorPowerFields(entityType));
        HashSet<PowersConfigFields> disabledPowers = EliteMobPowersConfig.getDisabledPowerFields(entityType);
        validMajorPowers.removeAll(disabledPowers);
        validDefensivePowers.removeAll(disabledPowers);
        validOffensivePowers.removeAll(disabledPowers);
        validMiscellaneousPowers.removeAll(disabledPowers);
    }

    public void removeOffensivePower(String filename) {
        validOffensivePowers.remove(PowersConfig.getPower(filename));
    }

    public void removeDefensivePower(String filename) {
        validDefensivePowers.remove(PowersConfig.getPower(filename));
    }

}
