package com.magmaguy.elitemobs.advancedcombat.abilities;

/** Fixed internal tuning. It is intentionally not populated from server configuration. */
public record AbilityTuning(
        double damageMultiplier,
        double healingFraction,
        double shieldFraction,
        double displacement,
        double modifierMultiplier,
        double range,
        double radius,
        int durationTicks,
        int repetitions,
        int projectileCount,
        double weakenMultiplier) {

    public AbilityTuning(
            double damageMultiplier,
            double healingFraction,
            double shieldFraction,
            double displacement,
            double modifierMultiplier,
            double range,
            double radius,
            int durationTicks,
            int repetitions,
            int projectileCount) {
        this(damageMultiplier, healingFraction, shieldFraction, displacement,
                modifierMultiplier, range, radius, durationTicks, repetitions, projectileCount, 1D);
    }

    public AbilityTuning {
        requireNonNegative(damageMultiplier, "damageMultiplier");
        requireNonNegative(healingFraction, "healingFraction");
        requireNonNegative(shieldFraction, "shieldFraction");
        requireNonNegative(displacement, "displacement");
        requireNonNegative(modifierMultiplier, "modifierMultiplier");
        requireNonNegative(range, "range");
        requireNonNegative(radius, "radius");
        if (!Double.isFinite(weakenMultiplier)
                || weakenMultiplier < 0D
                || weakenMultiplier > 1D)
            throw new IllegalArgumentException("weakenMultiplier must be within [0, 1]");
        if (durationTicks < 0 || repetitions < 1 || projectileCount < 1)
            throw new IllegalArgumentException("Durations and counts must be valid");
    }

    public static AbilityTuning combat(double damage, double range, double radius, int duration) {
        return new AbilityTuning(damage, 0, 0, 0, 1.0, range, radius, duration, 1, 1);
    }

    public static AbilityTuning support(double healing, double shield, double range, double radius, int duration) {
        return new AbilityTuning(0, healing, shield, 0, 1.0, range, radius, duration, 1, 1);
    }

    AbilityTuning withWeakenMultiplier(double multiplier) {
        return new AbilityTuning(damageMultiplier, healingFraction, shieldFraction, displacement,
                modifierMultiplier, range, radius, durationTicks, repetitions, projectileCount,
                multiplier);
    }

    private static void requireNonNegative(double value, String field) {
        if (!Double.isFinite(value) || value < 0)
            throw new IllegalArgumentException(field + " must be finite and non-negative");
    }
}
