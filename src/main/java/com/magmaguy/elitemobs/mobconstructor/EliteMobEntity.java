package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.items.MobTierFinder;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobpowers.majorpowers.MajorPowers;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPowers;
import com.magmaguy.elitemobs.powerstances.MajorPowerPowerStance;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EliteMobEntity {

    /*
    Note that a lot of values here are defined by EliteMobProperties.java
     */
    private LivingEntity eliteMob;
    private int eliteMobLevel;
    private double eliteMobTier;
    private double maxHealth;
    private String name;
    private ArrayList<MinorPowers> offensivePowers = new ArrayList<>();
    private ArrayList<MinorPowers> defensivePowers = new ArrayList<>();
    private ArrayList<MinorPowers> miscelleaneousPowers = new ArrayList<>();
    private ArrayList<MajorPowers> majorPowers = new ArrayList<>();
    /*
    This just defines default behavior
     */
    private boolean isUnalterable = false;
    private boolean canStack = true;
    private boolean hasCustomArmor = false;
    private boolean hasCustomName = false;
    private boolean hasCustomPowers = false;

    /*
    This is the generic constructor used in most instances of natural elite mob generation
     */
    public EliteMobEntity(LivingEntity livingEntity, int eliteMobLevel) {

        /*
        Register living entity to keep track of which entity this object is tied to
         */
        this.eliteMob = livingEntity;
        /*
        Register level, this is variable as per stacking rules
         */
        setEliteMobLevel(eliteMobLevel);
        eliteMobTier = MobTierFinder.findMobTier(eliteMobLevel);
        /*
        Get correct instance of plugin data, necessary for settings names and health among other things
         */
        EliteMobProperties eliteMobProperties = EliteMobProperties.getPluginData(livingEntity);
        /*
        Handle name, variable as per stacking rules
         */
        setCustomName(eliteMobProperties);
        /*
        Handle health, max is variable as per stacking rules
        Currently #setHealth() resets the health back to maximum
         */
        setMaxHealth(eliteMobProperties);
        setHealth();
        /*
        Set the armor
         */
        setArmor();
        /*
        Set the power list
         */
        setPowers(eliteMobProperties);
        /*
        Start tracking the entity
         */
        EntityTracker.registerEliteMob(this);

    }

    /*
    This is the generic constructor used in most instances of natural elite mob generation
     */
    public EliteMobEntity(LivingEntity livingEntity, int eliteMobLevel, List<MajorPowers> majorPowers,
                          List<MinorPowers> minorPowers, boolean isUnalterable) {

        /*
        Register living entity to keep track of which entity this object is tied to
         */
        this.eliteMob = livingEntity;
        /*
        Register level, this is variable as per stacking rules
         */
        setEliteMobLevel(eliteMobLevel);
        eliteMobTier = MobTierFinder.findMobTier(eliteMobLevel);
        /*
        Get correct instance of plugin data, necessary for settings names and health among other things
         */
        EliteMobProperties eliteMobProperties = EliteMobProperties.getPluginData(livingEntity);
        /*
        Handle name, variable as per stacking rules
         */
        setCustomName(eliteMobProperties);
        /*
        Handle health, max is variable as per stacking rules
        Currently #setHealth() resets the health back to maximum
         */
        setMaxHealth(eliteMobProperties);
        setHealth();
        /*
        Set the armor
         */
        setArmor();
        /*
        Set the power list
         */
        setPowers(eliteMobProperties);
        /*
        Set whether or not the entity should be altered
         */
        this.isUnalterable = isUnalterable;
        /*
        Start tracking the entity
         */
        EntityTracker.registerEliteMob(new EliteMobEntity(livingEntity, this.eliteMobLevel));

    }

    private void setCustomName(EliteMobProperties eliteMobProperties) {

        this.name = ChatColorConverter.convert(
                eliteMobProperties.getName().replace(
                        "$level", eliteMobLevel + ""));
        eliteMob.setCustomName(this.name);
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.ALWAYS_SHOW_NAMETAGS))
            eliteMob.setCustomNameVisible(true);
    }

    private void setEliteMobLevel(int newLevel) {
        if (newLevel < 1)
            newLevel = 1;
        this.eliteMobLevel = newLevel;
    }

    private void setMaxHealth(EliteMobProperties eliteMobProperties) {
        double defaultMaxHealth = eliteMobProperties.getDefaultMaxHealth();
        this.maxHealth = (eliteMobLevel * CombatSystem.PER_LEVEL_POWER_INCREASE * defaultMaxHealth + defaultMaxHealth);
        eliteMob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
    }

    private void setHealth() {
        eliteMob.setHealth(maxHealth);
    }

    private void setArmor() {

        if (VersionChecker.currentVersionIsUnder(12, 2)) return;
        if (!ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_ARMOR)) return;

        eliteMob.getEquipment().setItemInMainHandDropChance(0);
        eliteMob.getEquipment().setHelmetDropChance(0);
        eliteMob.getEquipment().setChestplateDropChance(0);
        eliteMob.getEquipment().setLeggingsDropChance(0);
        eliteMob.getEquipment().setBootsDropChance(0);

        if (hasCustomArmor) return;

        if (!(eliteMob instanceof Zombie || eliteMob instanceof PigZombie ||
                eliteMob instanceof Skeleton || eliteMob instanceof WitherSkeleton)) return;

        eliteMob.getEquipment().setBoots(new ItemStack(Material.AIR));
        eliteMob.getEquipment().setLeggings(new ItemStack(Material.AIR));
        eliteMob.getEquipment().setChestplate(new ItemStack(Material.AIR));
        eliteMob.getEquipment().setHelmet(new ItemStack(Material.AIR));

        if (eliteMobLevel >= 12)
            if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS))
                eliteMob.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));

        if (eliteMobLevel >= 14)
            eliteMob.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));

        if (eliteMobLevel >= 16)
            eliteMob.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));

        if (eliteMobLevel >= 18)
            eliteMob.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));

        if (eliteMobLevel >= 20)
            if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS))
                eliteMob.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));

        if (eliteMobLevel >= 22)
            eliteMob.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));

        if (eliteMobLevel >= 24)
            eliteMob.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));

        if (eliteMobLevel >= 26)
            eliteMob.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));

        if (eliteMobLevel >= 28)
            if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS))
                eliteMob.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));

        if (eliteMobLevel >= 30)
            eliteMob.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));

        if (eliteMobLevel >= 32)
            eliteMob.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));

        if (eliteMobLevel >= 34)
            eliteMob.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));

        if (eliteMobLevel >= 36)
            eliteMob.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));

        if (eliteMobLevel >= 38)
            if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS))
                eliteMob.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));

        if (eliteMobLevel >= 40)
            eliteMob.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));

        if (eliteMobLevel >= 42)
            eliteMob.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));

    }

    private void setPowers(EliteMobProperties eliteMobProperties) {

        if (hasCustomPowers) return;
        if (eliteMobTier < 1) return;

        int availableDefensivePowers = 0;
        int availableOffensivePowers = 0;
        int availableMiscellaneousPowers = 0;
        int availableMajorPowers = 0;

        if (eliteMobTier >= 1) availableDefensivePowers = 1;
        if (eliteMobTier >= 2) availableOffensivePowers = 1;
        if (eliteMobTier >= 3) availableMiscellaneousPowers = 1;
        if (eliteMobTier >= 4) availableMajorPowers = 1;
        if (eliteMobTier >= 5) availableDefensivePowers = 2;
        if (eliteMobTier >= 6) availableOffensivePowers = 2;
        if (eliteMobTier >= 7) availableMiscellaneousPowers = 2;
        if (eliteMobTier >= 8) availableMajorPowers = 2;


        //apply defensive powers
        applyMinorPowers((ArrayList<MinorPowers>) eliteMobProperties.validDefensivePowers.clone(), defensivePowers, availableDefensivePowers);

        //apply offensive powers
        applyMinorPowers((ArrayList<MinorPowers>) eliteMobProperties.validOffensivePowers.clone(), offensivePowers, availableOffensivePowers);

        //apply miscellaneous powers
        applyMinorPowers((ArrayList<MinorPowers>) eliteMobProperties.validMiscellaneousPowers.clone(), miscelleaneousPowers, availableMiscellaneousPowers);

        //apply major powers
        applyMajorPowers((ArrayList<MajorPowers>) eliteMobProperties.validMajorPowers.clone(), majorPowers, availableMajorPowers);

        MinorPowerPowerStance minorPowerStanceMath = new MinorPowerPowerStance();
        minorPowerStanceMath.itemEffect(eliteMob);

        if (availableMajorPowers > 0) {
            MajorPowerPowerStance majorPowerPowerStance = new MajorPowerPowerStance();
            majorPowerPowerStance.itemEffect(eliteMob);
        }

    }

    private static void applyMinorPowers(ArrayList<MinorPowers> configMinorPowers, ArrayList<MinorPowers> existingMinorPowers, int availablePowerAmount) {

        availablePowerAmount = availablePowerAmount - existingMinorPowers.size();

        if (availablePowerAmount < 1) return;

        ArrayList<MinorPowers> localMinorPowers = (ArrayList<MinorPowers>) configMinorPowers.clone();

        for (MinorPowers minorPower : existingMinorPowers)
            localMinorPowers.remove(minorPower);

        for (int i = 0; i < availablePowerAmount; i++)
            if (localMinorPowers.size() < 1)
                break;
            else {
                MinorPowers selectedMinorPower = localMinorPowers.get(ThreadLocalRandom.current().nextInt(localMinorPowers.size()));
                existingMinorPowers.add(selectedMinorPower);
                localMinorPowers.remove(selectedMinorPower);
            }

    }

    private static void applyMajorPowers(ArrayList<MajorPowers> configMajorPowers, ArrayList<MajorPowers> existingMajorPowers, int availablePowerAmount) {

        availablePowerAmount = availablePowerAmount - existingMajorPowers.size();

        if (availablePowerAmount < 1) return;

        ArrayList<MajorPowers> localMajorPowers = (ArrayList<MajorPowers>) configMajorPowers.clone();

        for (MajorPowers majorPower : existingMajorPowers)
            localMajorPowers.remove(majorPower);

        for (int i = 0; i < availablePowerAmount; i++)
            if (localMajorPowers.size() < 1)
                break;
            else {
                MajorPowers selectedMinorPower = localMajorPowers.get(ThreadLocalRandom.current().nextInt(localMajorPowers.size()));
                existingMajorPowers.add(selectedMinorPower);
                localMajorPowers.remove(selectedMinorPower);
            }

    }

    public boolean isThisEliteMob(LivingEntity livingEntity) {
        return livingEntity.equals(this.eliteMob);
    }

    public LivingEntity getLivingEntity() {
        return eliteMob;
    }

    public int getEliteMobLevel() {
        return eliteMobLevel;
    }

    public boolean hasPower(MinorPowers minorPower) {
        return offensivePowers.contains(minorPower) || defensivePowers.contains(minorPower) || miscelleaneousPowers.contains(minorPower);
    }

    public boolean hasPower(MajorPowers majorPower) {
        return majorPowers.contains(majorPower);
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setCanStack(boolean bool) {
        this.canStack = bool;
    }

    public boolean getHasCustomArmor() {
        return this.hasCustomArmor;
    }

    public void setHasCustomArmor(boolean bool) {
        this.hasCustomArmor = bool;
    }

    public boolean getHasCustomPowers() {
        return this.hasCustomPowers;
    }

    public void setHasCustomPowers(boolean bool) {
        this.hasCustomPowers = bool;
    }

    public String getName() {
        return this.name;
    }

}
