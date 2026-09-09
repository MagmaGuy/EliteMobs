package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

import java.util.Objects;

/**
 * Resolves catalog defaults against the native body's health before elite scaling.
 */
final class NativeMindActorDefaults {

    private static final double LEGACY_BASE_HEALTH = 20.0D;

    private NativeMindActorDefaults() {
    }

    static String nameTemplate(EntityType entityType) {
        Objects.requireNonNull(entityType, "entityType");
        return MobPropertiesConfig.defaultNameTemplate(entityType);
    }

    static double baseHealth(EliteMobProperties properties, double nativeBaseHealth) {
        if (properties != null && Double.isFinite(properties.getDefaultMaxHealth())
                && properties.getDefaultMaxHealth() > 0.0D) {
            return properties.getDefaultMaxHealth();
        }
        if (Double.isFinite(nativeBaseHealth) && nativeBaseHealth > 0.0D) {
            return nativeBaseHealth;
        }
        return LEGACY_BASE_HEALTH;
    }

    static void requireRandomizedPowerSupport(
            EntityType entityType,
            EliteMobProperties properties,
            boolean randomized) {
        if (randomized && properties == null) {
            throw new IllegalArgumentException(
                    "No configured elite properties for carrier " + entityType);
        }
    }
}
