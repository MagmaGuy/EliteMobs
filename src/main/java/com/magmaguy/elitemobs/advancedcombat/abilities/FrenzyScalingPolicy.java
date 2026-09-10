package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Objects;

/** Continuous missing-health curve shared by Frenzy's damage and movement bonuses. */
public final class FrenzyScalingPolicy {
    public static final double BASE_MAXIMUM_SPEED_ADJUSTMENT = .40D;

    private FrenzyScalingPolicy() {
    }

    /** Resolves the player-visible Frenzy uptime at the effective-level runtime boundary. */
    public static int durationTicks(AbilityTuning tuning, int effectiveLevel) {
        Objects.requireNonNull(tuning, "tuning");
        return ActiveAbilityLevelScaling.durationTicks(tuning.durationTicks(), effectiveLevel);
    }

    public static Scaling scale(
            double currentHealth,
            double maximumHealth,
            double maximumDamageBonus,
            double maximumSpeedAdjustment) {
        if (!Double.isFinite(maximumHealth) || maximumHealth <= 0D)
            throw new IllegalArgumentException("maximumHealth must be positive and finite");
        requireNonNegative(maximumDamageBonus, "maximumDamageBonus");
        requireNonNegative(maximumSpeedAdjustment, "maximumSpeedAdjustment");

        double boundedHealth = Double.isFinite(currentHealth)
                ? Math.max(0D, Math.min(maximumHealth, currentHealth))
                : maximumHealth;
        double missingHealthFraction = 1D - boundedHealth / maximumHealth;
        return new Scaling(
                missingHealthFraction,
                1D + maximumDamageBonus * missingHealthFraction,
                maximumSpeedAdjustment * missingHealthFraction);
    }

    private static void requireNonNegative(double value, String field) {
        if (!Double.isFinite(value) || value < 0D)
            throw new IllegalArgumentException(field + " must be finite and non-negative");
    }

    public record Scaling(
            double missingHealthFraction,
            double damageMultiplier,
            double movementSpeedAdjustment) {
    }
}
