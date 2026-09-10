package com.magmaguy.elitemobs.advancedcombat;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Compact numeric health text that never rounds a wounded player up to apparent full health. */
public final class CombatHealthFormatter {

    private static final int DECIMAL_PLACES = 2;

    private CombatHealthFormatter() {
    }

    public static String format(double health) {
        if (!Double.isFinite(health) || health <= 0D) return "0";
        return BigDecimal.valueOf(health)
                .setScale(DECIMAL_PLACES, RoundingMode.DOWN)
                .stripTrailingZeros()
                .toPlainString();
    }
}
