package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Objects;

/** Pure composition rules for class healing amount and lifetime modifiers. */
final class AbilityHealingPolicy {
    private AbilityHealingPolicy() {
    }

    static double multiplier(
            AbilityMechanicModifiers modifiers,
            AbilityHealingType type,
            int qualifiedEnemyCount) {
        Objects.requireNonNull(modifiers, "modifiers");
        double typed = switch (Objects.requireNonNull(type, "type")) {
            case BURST -> modifiers.burstHealingMultiplier();
            case PERIODIC -> modifiers.periodicHealingMultiplier();
        };
        return typed * (qualifiedEnemyCount >= 2
                ? modifiers.groupedEnemyHealingMultiplier()
                : 1D);
    }

    static int durationTicks(
            int authoredTicks,
            AbilityHealingType type,
            AbilityMechanicModifiers modifiers) {
        if (authoredTicks <= 0) return 0;
        double multiplier = type == AbilityHealingType.PERIODIC
                ? modifiers.periodicDurationMultiplier()
                : 1D;
        return (int) Math.min(Integer.MAX_VALUE,
                Math.max(1L, Math.round(authoredTicks * multiplier)));
    }
}
