package com.magmaguy.elitemobs.experimentalcombat.abilities;

/**
 * Execution metadata for mechanics that do not belong in the generic effect set.
 * Keeping these traits on the ability definition prevents the engine from recognizing
 * individual class or form IDs.
 */
public record AbilityExecutionTraits(
        boolean establishesMobilityAnchor,
        double selfHealingMultiplier,
        java.util.Set<AbilityMechanic> mechanics) {
    public static final AbilityExecutionTraits STANDARD =
            new AbilityExecutionTraits(false, 1D, java.util.Set.of());

    public AbilityExecutionTraits {
        if (!Double.isFinite(selfHealingMultiplier) || selfHealingMultiplier < 0D)
            throw new IllegalArgumentException("selfHealingMultiplier must be finite and non-negative");
        mechanics = java.util.Set.copyOf(mechanics);
    }

    public static AbilityExecutionTraits mobilityAnchor() {
        return new AbilityExecutionTraits(true, 1D, java.util.Set.of());
    }

    public static AbilityExecutionTraits reducedSelfHealing(double multiplier) {
        return new AbilityExecutionTraits(false, multiplier, java.util.Set.of());
    }

    public static AbilityExecutionTraits mechanics(AbilityMechanic... mechanics) {
        return new AbilityExecutionTraits(false, 1D,
                mechanics.length == 0
                        ? java.util.Set.of()
                        : java.util.EnumSet.of(mechanics[0], mechanics));
    }

    public static AbilityExecutionTraits mobilityAnchor(AbilityMechanic... mechanics) {
        return new AbilityExecutionTraits(true, 1D,
                mechanics.length == 0
                        ? java.util.Set.of()
                        : java.util.EnumSet.of(mechanics[0], mechanics));
    }

    public AbilityExecutionTraits plus(AbilityMechanic... additional) {
        if (additional.length == 0) return this;
        java.util.EnumSet<AbilityMechanic> combined = mechanics.isEmpty()
                ? java.util.EnumSet.noneOf(AbilityMechanic.class)
                : java.util.EnumSet.copyOf(mechanics);
        java.util.Collections.addAll(combined, additional);
        return new AbilityExecutionTraits(establishesMobilityAnchor, selfHealingMultiplier, combined);
    }
}
