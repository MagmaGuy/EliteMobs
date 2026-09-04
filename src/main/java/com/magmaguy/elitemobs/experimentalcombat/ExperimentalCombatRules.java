package com.magmaguy.elitemobs.experimentalcombat;

/** Fixed, versioned balance baseline for the experimental phase. */
public final class ExperimentalCombatRules {

    public static final double VANILLA_BASE_MAX_HEALTH = 20D;
    public static final double EXPERIMENTAL_BASE_MAX_HEALTH = 50D;
    public static final double FOOD_SATURATION_TO_HEALTH = 1D;
    public static final double CAKE_SLICE_SATURATION = 0.4D;
    public static final double OUT_OF_COMBAT_HEAL_FRACTION_PER_SECOND = 0.01D;
    public static final int SERVER_FOOD_LEVEL = 19;
    public static final int DISPLAY_FOOD_LEVEL = 20;
    public static final int RECONCILIATION_INTERVAL_TICKS = 20;
    public static final long COMBAT_TIMEOUT_TICKS = 20L * 20L;

    private ExperimentalCombatRules() {
    }

    public static double maxHealthFlatIncrease() {
        return EXPERIMENTAL_BASE_MAX_HEALTH - VANILLA_BASE_MAX_HEALTH;
    }

    public static double experimentalMaximumHealth(double ordinaryMaximumHealth) {
        return ordinaryMaximumHealth + maxHealthFlatIncrease();
    }

    public static double ordinaryMaximumHealth(double experimentalMaximumHealth) {
        return Math.max(1D, experimentalMaximumHealth - maxHealthFlatIncrease());
    }
}
