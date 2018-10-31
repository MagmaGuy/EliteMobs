package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobpowers.majorpowers.MajorPowers;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPowers;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class EliteMobEntity {

    /*
    Note that a lot of values here are defined by EliteMobProperties.java
     */
    private LivingEntity eliteMob;
    private int eliteMobLevel;
    private double maxHealth;
    private double currentHealth;
    private List<MinorPowers> minorPowersList = new ArrayList<>();
    private List<MajorPowers> majorPowersList = new ArrayList<>();
    /*
    This just defines default behavior
     */
    private boolean isUnalterable = false;
    private boolean canStack = true;
    private boolean hasCustomArmor = false;
    private boolean hasCustomName = false;

    public EliteMobEntity(LivingEntity livingEntity, int eliteMobLevel) {

        /*
        Register living entity to keep track of which entity this object is tied to
         */
        this.eliteMob = livingEntity;
        /*
        Register level, this is variable as per stacking rules
         */
        this.eliteMobLevel = eliteMobLevel;
        if (eliteMobLevel < 1)
            eliteMobLevel = 1;
        /*
        Get correct instance of plugin data, necessary for settings names and health among other things
         */
        EliteMobProperties eliteMobProperties = EliteMobProperties.getPluginData(livingEntity);
        /*
        Handle name, variable as per stacking rules
         */
        livingEntity.setCustomName(
                ChatColorConverter.convert(
                        eliteMobProperties.getName().replace(
                                "$level", eliteMobLevel + "")));
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.ALWAYS_SHOW_NAMETAGS))
            livingEntity.setCustomNameVisible(true);
        /*
        Handle health, max is variable as per stacking rules
        Currently #setHealth() resets the health back to maximum
         */
        setMaxHealth(eliteMobProperties);
        setHealth();

    }

    public void setEliteMobLevel(int newLevel) {
        if (newLevel < 1)
            newLevel = 1;
        this.eliteMobLevel = newLevel;
    }

    private void setMaxHealth(EliteMobProperties eliteMobProperties) {
        double defaultMaxHealth = eliteMobProperties.getDefaultMaxHealth();
        this.maxHealth = (eliteMobLevel * DamageAdjuster.PER_LEVEL_POWER_INCREASE * defaultMaxHealth + defaultMaxHealth);
        eliteMob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
    }

    private void setHealth() {
        this.currentHealth = eliteMob.getHealth();
        this.setHealth(maxHealth);
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
        return minorPowersList.contains(minorPower);
    }

    public boolean hasPower(MajorPowers majorPower) {
        return majorPowersList.contains(majorPower);
    }

    public void registerPower(MinorPowers minorPowers) {
        this.minorPowersList.add(minorPowers);
    }

    public void registerPower(MajorPowers majorPowers) {
        this.majorPowersList.add(majorPowers);
    }

    public double getHealth() {
        return currentHealth;
    }

    public void setHealth(double newHealth) {
        this.currentHealth = newHealth;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setCanStack(boolean bool) {
        this.canStack = bool;
    }

    public void setHasCustomArmor(boolean bool) {
        this.hasCustomArmor = bool;
    }


}
