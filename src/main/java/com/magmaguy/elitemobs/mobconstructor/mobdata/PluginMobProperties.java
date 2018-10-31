package com.magmaguy.elitemobs.mobconstructor.mobdata;

import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs.SuperMobProperties;
import org.bukkit.entity.EntityType;

public abstract class PluginMobProperties {

    public boolean isEnabled;
    public String name;
    public EntityType entityType;
    public double defaultMaxHealth;

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getName() {
        return name;
    }

    public EntityType getEntityType() {
        return entityType;
    }


    public double getDefaultMaxHealth() {
        return defaultMaxHealth;
    }

    public static void initializePluginMobValues() {
        SuperMobProperties.initializeSuperMobValues();
        EliteMobProperties.initializeEliteMobValues();
    }

}
