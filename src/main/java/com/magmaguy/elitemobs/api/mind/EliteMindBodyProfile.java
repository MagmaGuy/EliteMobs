package com.magmaguy.elitemobs.api.mind;

import org.bukkit.NamespacedKey;

import java.util.Objects;

/** Classloader-safe physical and locomotion request for a native Mind body. */
public record EliteMindBodyProfile(
        EliteMindBodyLocomotion locomotion,
        double uniformScale,
        boolean entityCollidable,
        NamespacedKey carrierType) {

    public static final NamespacedKey DEFAULT_CARRIER_TYPE = NamespacedKey.minecraft("zombie");

    public static final EliteMindBodyProfile GROUNDED =
            new EliteMindBodyProfile(
                    EliteMindBodyLocomotion.GROUNDED,
                    1.0D,
                    true,
                    DEFAULT_CARRIER_TYPE);

    /** Source-compatible constructor for the historical neutral zombie carrier. */
    public EliteMindBodyProfile(
            EliteMindBodyLocomotion locomotion,
            double uniformScale,
            boolean entityCollidable) {
        this(locomotion, uniformScale, entityCollidable, DEFAULT_CARRIER_TYPE);
    }

    public EliteMindBodyProfile {
        Objects.requireNonNull(locomotion, "locomotion");
        if (!Double.isFinite(uniformScale) || uniformScale <= 0.0D) {
            throw new IllegalArgumentException("uniformScale must be finite and positive");
        }
        Objects.requireNonNull(carrierType, "carrierType");
    }

    public static EliteMindBodyProfile standard(EliteMindBodyLocomotion locomotion) {
        return new EliteMindBodyProfile(locomotion, 1.0D, true, DEFAULT_CARRIER_TYPE);
    }

    public static EliteMindBodyProfile forCarrier(
            NamespacedKey carrierType,
            EliteMindBodyLocomotion locomotion) {
        return new EliteMindBodyProfile(locomotion, 1.0D, true, carrierType);
    }
}
