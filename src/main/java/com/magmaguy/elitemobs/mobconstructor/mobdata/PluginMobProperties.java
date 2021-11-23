package com.magmaguy.elitemobs.mobconstructor.mobdata;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs.SuperMobProperties;
import org.bukkit.entity.EntityType;

public abstract class PluginMobProperties {

    public boolean isEnabled;
    public String name;
    public EntityType entityType;
    public double defaultMaxHealth;
    public double baseDamage;

    public static void initializePluginMobValues() {
        SuperMobProperties.initializeSuperMobValues();
        EliteMobProperties.initializeEliteMobValues();
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getName() {
        return this.name;
    }

    public String getName(int level) {
        return this.name.replace("$level", level + "");
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

}
