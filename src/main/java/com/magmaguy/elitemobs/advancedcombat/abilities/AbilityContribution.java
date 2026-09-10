package com.magmaguy.elitemobs.advancedcombat.abilities;

/**
 * Actual immediate value produced by an ability execution. Delayed effects are reported through
 * {@link AbilitySemantics#recordContribution} when they occur.
 */
public record AbilityContribution(
        double damage,
        double effectiveHealing,
        double mitigatedDamage,
        double threat,
        int controlledEnemies,
        int supportedAllies,
        double movementDistance) {

    public static final AbilityContribution NONE = new AbilityContribution(0, 0, 0, 0, 0, 0, 0);

    public AbilityContribution {
        requireNonNegativeFinite(damage, "damage");
        requireNonNegativeFinite(effectiveHealing, "effectiveHealing");
        requireNonNegativeFinite(mitigatedDamage, "mitigatedDamage");
        requireNonNegativeFinite(threat, "threat");
        requireNonNegativeFinite(movementDistance, "movementDistance");
        if (controlledEnemies < 0) throw new IllegalArgumentException("controlledEnemies must not be negative");
        if (supportedAllies < 0) throw new IllegalArgumentException("supportedAllies must not be negative");
    }

    public AbilityContribution plus(AbilityContribution other) {
        if (other == null) return this;
        return new AbilityContribution(
                damage + other.damage,
                effectiveHealing + other.effectiveHealing,
                mitigatedDamage + other.mitigatedDamage,
                threat + other.threat,
                controlledEnemies + other.controlledEnemies,
                supportedAllies + other.supportedAllies,
                movementDistance + other.movementDistance);
    }

    public boolean isMeaningful() {
        return damage > 0 || effectiveHealing > 0 || mitigatedDamage > 0 || threat > 0;
    }

    private static void requireNonNegativeFinite(double value, String field) {
        if (!Double.isFinite(value) || value < 0)
            throw new IllegalArgumentException(field + " must be finite and non-negative");
    }
}
