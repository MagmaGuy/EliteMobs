package com.magmaguy.elitemobs.mobconstructor.mobdata;

import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import org.bukkit.entity.EntityType;

public abstract class PluginMobProperties {

    public final boolean isEnabled;
    public final String name;
    public final EntityType entityType;
    public final double defaultMaxHealth;
    public final double baseDamage;

    protected PluginMobProperties(boolean isEnabled, String name, EntityType entityType,
                                  double defaultMaxHealth, double baseDamage) {
        this.isEnabled = isEnabled;
        this.name = name;
        this.entityType = entityType;
        this.defaultMaxHealth = defaultMaxHealth;
        this.baseDamage = baseDamage;
    }

    public static void initializePluginMobValues() {
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

}
