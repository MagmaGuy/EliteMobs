package com.magmaguy.elitemobs.experimentalcombat.abilities;

/** Shared level curve for class-ability effects and their balance analytics. */
public final class AbilityLevelScaling {
    private static final double BONUS_PER_LEVEL = .0025D;

    private AbilityLevelScaling() {
    }

    public static double multiplier(int effectiveLevel) {
        return 1D + Math.max(1, effectiveLevel) * BONUS_PER_LEVEL;
    }
}
