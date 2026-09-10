package com.magmaguy.elitemobs.advancedcombat.abilities;

/** Pure calculations for stateful abilities, kept free of Bukkit registry initialization. */
final class AbilityStateMath {
    private AbilityStateMath() {
    }

    static double deathGuardDamageCeiling(double health, double absorption) {
        if (!Double.isFinite(health) || !Double.isFinite(absorption)) return 0D;
        return Math.max(0D, Math.max(0D, health) + Math.max(0D, absorption) - 1D);
    }

    static double retaliationHealing(double storedDamage, double healingFraction) {
        if (!Double.isFinite(storedDamage) || !Double.isFinite(healingFraction)) return 0D;
        return Math.max(0D, storedDamage) * Math.max(0D, healingFraction);
    }

    static double healthScaledMark(double baseMultiplier, double targetHealthFraction) {
        if (!Double.isFinite(baseMultiplier) || !Double.isFinite(targetHealthFraction)) return 1D;
        double baseBonus = Math.max(0D, baseMultiplier - 1D);
        double missing = 1D - Math.max(0D, Math.min(1D, targetHealthFraction));
        return 1D + baseBonus * (1D + missing * 1.5D);
    }

    static boolean breaksOwnedBarrier(double incomingDamage, double ownedAbsorptionRemaining) {
        return Double.isFinite(incomingDamage)
                && Double.isFinite(ownedAbsorptionRemaining)
                && ownedAbsorptionRemaining > 0D
                && incomingDamage >= ownedAbsorptionRemaining;
    }

    static boolean isMajorThreat(double incomingDamage, double health, double maximumHealth) {
        if (!Double.isFinite(incomingDamage) || !Double.isFinite(health)
                || !Double.isFinite(maximumHealth) || maximumHealth <= 0D) return false;
        return incomingDamage >= maximumHealth * .15D || incomingDamage >= health;
    }
}
