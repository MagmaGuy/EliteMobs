package com.magmaguy.elitemobs.mobconstructor.mobdata;

import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs.SuperMobProperties;
import com.magmaguy.elitemobs.config.DefaultConfig;
import org.bukkit.entity.EntityType;

public abstract class PluginMobProperties {

    public boolean isEnabled;
    public String name;
    public EntityType entityType;
    public double defaultMaxHealth;
    public double baseDamage;

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getName() {
        return this.name;
    }

    public EntityType getEntityType() {
        return this.entityType;
    }


    public double getDefaultMaxHealth() {
        return defaultMaxHealth;
    }

    public double getSuperMobMaxHealth() {
        return defaultMaxHealth * DefaultConfig.superMobStackAmount;
    }

    public static void initializePluginMobValues() {
        SuperMobProperties.initializeSuperMobValues();
        EliteMobProperties.initializeEliteMobValues();
    }

}
