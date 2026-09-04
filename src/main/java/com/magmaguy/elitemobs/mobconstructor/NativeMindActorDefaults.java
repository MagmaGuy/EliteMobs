package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Defaults for native Mind carriers that do not belong to the historical natural-elite catalog.
 * Exact Mind power loadouts may use every living carrier without fabricating a parallel mob-data
 * registry; native health is preserved and the fallback name remains deterministic.
 */
final class NativeMindActorDefaults {

    private static final double LEGACY_BASE_HEALTH = 20.0D;

    private NativeMindActorDefaults() {
    }

    static String nameTemplate(EntityType entityType) {
        Objects.requireNonNull(entityType, "entityType");
        String readableType = Arrays.stream(entityType.name().toLowerCase(Locale.ROOT).split("_"))
                .filter(part -> !part.isEmpty())
                .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .collect(Collectors.joining(" "));
        return "&fLvl &2$level &fElite &2" + readableType;
    }

    static double baseHealth(EliteMobProperties properties, double nativeBaseHealth) {
        if (properties != null) {
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
                    "Randomized EliteMobs powers require configured mob properties for carrier "
                            + entityType + "; use an exact Mind power loadout instead");
        }
    }
}
