package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.advancedcombat.presentation.ClassPresentationTheme;

/** Pure text composition for damage popup amounts. */
final class CombatPopupText {
    private CombatPopupText() {
    }

    static String damageAmount(
            boolean scaledCombat,
            double maximumHealth,
            double totalDamage,
            double classAbilityBonusDamage) {
        String total = amount(scaledCombat, maximumHealth, totalDamage);
        if (!Double.isFinite(classAbilityBonusDamage) || classAbilityBonusDamage <= 1.0E-6D)
            return total;
        String bonus = amount(scaledCombat, maximumHealth, classAbilityBonusDamage);
        return total + " &8[" + ClassPresentationTheme.abilityDamage("+" + bonus) + "&8]";
    }

    private static String amount(boolean scaledCombat, double maximumHealth, double damage) {
        return scaledCombat
                ? DisplayTextFormatter.percentage(damage / Math.max(1D, maximumHealth))
                : DisplayTextFormatter.number(damage);
    }
}
