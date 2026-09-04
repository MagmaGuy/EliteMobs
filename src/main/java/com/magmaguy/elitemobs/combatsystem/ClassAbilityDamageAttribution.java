package com.magmaguy.elitemobs.combatsystem;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/** Tracks the counterfactual damage of a hit without its active class-ability multipliers. */
public final class ClassAbilityDamageAttribution {
    private final Set<String> sources = new LinkedHashSet<>();
    private double damageWithoutAbilities = Double.NaN;

    /**
     * Returns the damage after one real ability multiplier and remembers its exact pre-buff value.
     * Invalid or non-increasing multipliers leave both damage and attribution untouched.
     */
    public double applyMultiplier(String abilityId, double currentDamage, double multiplier) {
        Objects.requireNonNull(abilityId, "abilityId");
        if (abilityId.isBlank()
                || !Double.isFinite(currentDamage)
                || currentDamage < 0D
                || !Double.isFinite(multiplier)
                || multiplier <= 1D)
            return currentDamage;

        double modifiedDamage = currentDamage * multiplier;
        if (!Double.isFinite(modifiedDamage)) return currentDamage;
        if (!isTracking()) damageWithoutAbilities = currentDamage;
        sources.add(abilityId);
        return modifiedDamage;
    }

    /** Keeps the counterfactual aligned when a later combat listener scales or clamps the hit. */
    public void observeReplacement(double previousDamage, double replacementDamage) {
        if (!isTracking()) return;
        if (!Double.isFinite(previousDamage) || !Double.isFinite(replacementDamage)) {
            clear();
            return;
        }
        if (previousDamage <= 0D || replacementDamage <= 0D) {
            damageWithoutAbilities = Math.max(0D, replacementDamage);
            return;
        }
        damageWithoutAbilities = Math.max(0D, damageWithoutAbilities * replacementDamage / previousDamage);
    }

    public double bonusDamage(double finalDamage) {
        if (!isTracking() || !Double.isFinite(finalDamage) || finalDamage <= 0D) return 0D;
        return Math.max(0D, finalDamage - damageWithoutAbilities);
    }

    public Set<String> sources() {
        return Set.copyOf(sources);
    }

    private boolean isTracking() {
        return Double.isFinite(damageWithoutAbilities);
    }

    private void clear() {
        damageWithoutAbilities = Double.NaN;
        sources.clear();
    }
}
