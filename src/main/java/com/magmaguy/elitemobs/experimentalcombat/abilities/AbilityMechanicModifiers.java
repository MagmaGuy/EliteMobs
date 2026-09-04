package com.magmaguy.elitemobs.experimentalcombat.abilities;

/**
 * Ability-facing typed modifiers. The active engine owns this boundary so passive content can
 * influence reusable mechanics without leaking passive implementation types into execution.
 */
public record AbilityMechanicModifiers(
        double burstHealingMultiplier,
        double periodicHealingMultiplier,
        double groupedEnemyHealingMultiplier,
        double periodicDurationMultiplier,
        double shieldStrengthMultiplier,
        double redirectedDamageMultiplier,
        double controlDurationMultiplier,
        double controlPotencyMultiplier) {

    public static final AbilityMechanicModifiers NEUTRAL = new AbilityMechanicModifiers(
            1D, 1D, 1D, 1D, 1D, 1D, 1D, 1D);

    public AbilityMechanicModifiers {
        burstHealingMultiplier = valid(burstHealingMultiplier, "burstHealingMultiplier");
        periodicHealingMultiplier = valid(periodicHealingMultiplier, "periodicHealingMultiplier");
        groupedEnemyHealingMultiplier = valid(
                groupedEnemyHealingMultiplier, "groupedEnemyHealingMultiplier");
        periodicDurationMultiplier = valid(periodicDurationMultiplier, "periodicDurationMultiplier");
        shieldStrengthMultiplier = valid(shieldStrengthMultiplier, "shieldStrengthMultiplier");
        redirectedDamageMultiplier = valid(redirectedDamageMultiplier, "redirectedDamageMultiplier");
        controlDurationMultiplier = valid(controlDurationMultiplier, "controlDurationMultiplier");
        controlPotencyMultiplier = valid(controlPotencyMultiplier, "controlPotencyMultiplier");
    }

    private static double valid(double value, String name) {
        if (!Double.isFinite(value) || value <= 0D)
            throw new IllegalArgumentException(name + " must be finite and positive");
        return Math.max(.25D, Math.min(3D, value));
    }
}
