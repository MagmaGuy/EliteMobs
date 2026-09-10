package com.magmaguy.elitemobs.advancedcombat;

/** Fixed, versioned balance baseline for the advanced phase. */
public final class AdvancedCombatRules {

    public static final double VANILLA_BASE_MAX_HEALTH = 20D;
    public static final double ADVANCED_BASE_MAX_HEALTH = 50D;
    public static final double FOOD_SATURATION_TO_HEALTH = 1D;
    public static final double CAKE_SLICE_SATURATION = 0.4D;
    public static final double OUT_OF_COMBAT_HEAL_FRACTION_PER_SECOND = 0.01D;
    public static final int SERVER_FOOD_LEVEL = 19;
    public static final int DISPLAY_FOOD_LEVEL = 20;
    public static final int RECONCILIATION_INTERVAL_TICKS = 20;
    public static final long COMBAT_TIMEOUT_TICKS = 20L * 20L;

    private AdvancedCombatRules() {
    }

    public static double maxHealthFlatIncrease() {
        return ADVANCED_BASE_MAX_HEALTH - VANILLA_BASE_MAX_HEALTH;
    }

    public static double advancedMaximumHealth(double ordinaryMaximumHealth) {
        return ordinaryMaximumHealth + maxHealthFlatIncrease();
    }

    public static double ordinaryMaximumHealth(double advancedMaximumHealth) {
        return Math.max(1D, advancedMaximumHealth - maxHealthFlatIncrease());
    }
}
