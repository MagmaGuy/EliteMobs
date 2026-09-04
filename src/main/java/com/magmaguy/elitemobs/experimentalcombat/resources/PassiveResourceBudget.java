package com.magmaguy.elitemobs.experimentalcombat.resources;

import java.util.Objects;

/** Pure time-to-resource calculations for the once-per-second class-resource policy tick. */
public final class PassiveResourceBudget {
    private PassiveResourceBudget() {
    }

    public static double secondsToFill(ClassResourceDefinition definition, boolean inCombat) {
        Objects.requireNonNull(definition, "definition");
        return secondsToAfford(definition, definition.initialAmount(), definition.maximum(), inCombat);
    }

    public static double secondsToAfford(
            ClassResourceDefinition definition,
            double currentAmount,
            double cost,
            boolean inCombat) {
        Objects.requireNonNull(definition, "definition");
        if (!Double.isFinite(currentAmount) || !Double.isFinite(cost) || currentAmount < 0D || cost < 0D)
            throw new IllegalArgumentException("Resource amount and cost must be finite and non-negative");
        double missing = Math.max(0D, Math.min(definition.maximum(), cost) - currentAmount);
        if (missing == 0D) return 0D;
        double gainPerSecond = inCombat
                ? definition.inCombatTickDelta()
                : definition.outOfCombatTickDelta();
        return gainPerSecond > 0D ? missing / gainPerSecond : Double.POSITIVE_INFINITY;
    }
}
