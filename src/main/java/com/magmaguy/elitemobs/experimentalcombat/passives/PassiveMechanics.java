package com.magmaguy.elitemobs.experimentalcombat.passives;

/**
 * Typed passive modifiers for mechanics that cannot be represented as generic combat stats.
 * Multipliers compose multiplicatively. Resistance composes by multiplying the remaining effect,
 * and party movement remains an additive attribute adjustment.
 */
public record PassiveMechanics(
        double burstHealingMultiplier,
        double periodicHealingMultiplier,
        double groupedEnemyHealingMultiplier,
        double periodicDurationMultiplier,
        double shieldStrengthMultiplier,
        double redirectedDamageMultiplier,
        double controlDurationMultiplier,
        double controlPotencyMultiplier,
        double abilityCostMultiplier,
        double controlResistanceFraction,
        double partyMovementSpeedAdjustment) {

    public static final PassiveMechanics NEUTRAL = new PassiveMechanics(
            1D, 1D, 1D, 1D, 1D, 1D, 1D, 1D, 1D, 0D, 0D);

    public PassiveMechanics {
        burstHealingMultiplier = multiplier(burstHealingMultiplier, "burstHealingMultiplier");
        periodicHealingMultiplier = multiplier(periodicHealingMultiplier, "periodicHealingMultiplier");
        groupedEnemyHealingMultiplier = multiplier(
                groupedEnemyHealingMultiplier, "groupedEnemyHealingMultiplier");
        periodicDurationMultiplier = multiplier(periodicDurationMultiplier, "periodicDurationMultiplier");
        shieldStrengthMultiplier = multiplier(shieldStrengthMultiplier, "shieldStrengthMultiplier");
        redirectedDamageMultiplier = multiplier(redirectedDamageMultiplier, "redirectedDamageMultiplier");
        controlDurationMultiplier = multiplier(controlDurationMultiplier, "controlDurationMultiplier");
        controlPotencyMultiplier = multiplier(controlPotencyMultiplier, "controlPotencyMultiplier");
        abilityCostMultiplier = multiplier(abilityCostMultiplier, "abilityCostMultiplier");
        if (!Double.isFinite(controlResistanceFraction)
                || controlResistanceFraction < 0D
                || controlResistanceFraction >= 1D)
            throw new IllegalArgumentException("controlResistanceFraction must be in [0, 1)");
        if (!Double.isFinite(partyMovementSpeedAdjustment))
            throw new IllegalArgumentException("partyMovementSpeedAdjustment must be finite");
    }

    /** Compatibility shape for mechanics that do not alter ability cost. */
    public PassiveMechanics(
            double burstHealingMultiplier,
            double periodicHealingMultiplier,
            double groupedEnemyHealingMultiplier,
            double periodicDurationMultiplier,
            double shieldStrengthMultiplier,
            double redirectedDamageMultiplier,
            double controlDurationMultiplier,
            double controlPotencyMultiplier,
            double controlResistanceFraction,
            double partyMovementSpeedAdjustment) {
        this(burstHealingMultiplier, periodicHealingMultiplier, groupedEnemyHealingMultiplier,
                periodicDurationMultiplier, shieldStrengthMultiplier, redirectedDamageMultiplier,
                controlDurationMultiplier, controlPotencyMultiplier, 1D,
                controlResistanceFraction, partyMovementSpeedAdjustment);
    }

    public PassiveMechanics combine(PassiveMechanics other) {
        if (other == null) throw new IllegalArgumentException("other must not be null");
        return new PassiveMechanics(
                boundedMultiplier(burstHealingMultiplier * other.burstHealingMultiplier),
                boundedMultiplier(periodicHealingMultiplier * other.periodicHealingMultiplier),
                boundedMultiplier(groupedEnemyHealingMultiplier * other.groupedEnemyHealingMultiplier),
                boundedMultiplier(periodicDurationMultiplier * other.periodicDurationMultiplier),
                boundedMultiplier(shieldStrengthMultiplier * other.shieldStrengthMultiplier),
                boundedMultiplier(redirectedDamageMultiplier * other.redirectedDamageMultiplier),
                boundedMultiplier(controlDurationMultiplier * other.controlDurationMultiplier),
                boundedMultiplier(controlPotencyMultiplier * other.controlPotencyMultiplier),
                boundedMultiplier(abilityCostMultiplier * other.abilityCostMultiplier),
                Math.min(.85D, 1D - (1D - controlResistanceFraction)
                        * (1D - other.controlResistanceFraction)),
                Math.max(-.25D, Math.min(.25D,
                        partyMovementSpeedAdjustment + other.partyMovementSpeedAdjustment)));
    }

    /** Scales every authored delta from its neutral value. */
    PassiveMechanics scaled(double factor) {
        if (!Double.isFinite(factor) || factor < 0D)
            throw new IllegalArgumentException("factor must be finite and non-negative");
        return new PassiveMechanics(
                scaledMultiplier(burstHealingMultiplier, factor),
                scaledMultiplier(periodicHealingMultiplier, factor),
                scaledMultiplier(groupedEnemyHealingMultiplier, factor),
                scaledMultiplier(periodicDurationMultiplier, factor),
                scaledMultiplier(shieldStrengthMultiplier, factor),
                scaledMultiplier(redirectedDamageMultiplier, factor),
                scaledMultiplier(controlDurationMultiplier, factor),
                scaledMultiplier(controlPotencyMultiplier, factor),
                scaledMultiplier(abilityCostMultiplier, factor),
                Math.min(.85D, controlResistanceFraction * factor),
                Math.max(-.25D, Math.min(.25D, partyMovementSpeedAdjustment * factor)));
    }

    private static double scaledMultiplier(double value, double factor) {
        return boundedMultiplier(1D + (value - 1D) * factor);
    }

    private static double multiplier(double value, String name) {
        if (!Double.isFinite(value) || value <= 0D)
            throw new IllegalArgumentException(name + " must be finite and positive");
        return boundedMultiplier(value);
    }

    private static double boundedMultiplier(double value) {
        return Math.max(.25D, Math.min(3D, value));
    }
}
