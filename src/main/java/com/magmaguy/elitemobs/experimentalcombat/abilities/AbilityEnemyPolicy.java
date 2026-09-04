package com.magmaguy.elitemobs.experimentalcombat.abilities;

import java.util.Set;

/** Pure target predicates shared by ray tracing, area selection and delayed impacts. */
final class AbilityEnemyPolicy {
    static final double WOUNDED_HEALTH_FRACTION = .50D;

    private AbilityEnemyPolicy() {
    }

    static boolean permits(Set<AbilityMechanic> mechanics, EnemyFacts target) {
        if (mechanics.contains(AbilityMechanic.WOUNDED_TARGETS_ONLY)
                && target.healthFraction() >= WOUNDED_HEALTH_FRACTION) return false;
        if (mechanics.contains(AbilityMechanic.BOSS_ONLY) && !target.boss()) return false;
        return !mechanics.contains(AbilityMechanic.LARGE_OR_BOSS_ONLY)
                || target.boss()
                || target.large();
    }

    /** Traits whose empty result is authoritative and must never fall back to arbitrary enemies. */
    static boolean requiresQualifiedSelection(Set<AbilityMechanic> mechanics) {
        return mechanics.contains(AbilityMechanic.RECENT_ATTACKERS)
                || mechanics.contains(AbilityMechanic.TAUNTED_TARGETS_ONLY)
                || mechanics.contains(AbilityMechanic.REQUIRES_DEFENSE_BREAK)
                || mechanics.contains(AbilityMechanic.WOUNDED_TARGETS_ONLY)
                || mechanics.contains(AbilityMechanic.LARGE_OR_BOSS_ONLY)
                || mechanics.contains(AbilityMechanic.BOSS_ONLY)
                || mechanics.contains(AbilityMechanic.MINIMUM_RANGE);
    }

    record EnemyFacts(double healthFraction, boolean boss, boolean large) {
        EnemyFacts {
            if (!Double.isFinite(healthFraction)) healthFraction = 1D;
            healthFraction = Math.max(0D, Math.min(1D, healthFraction));
        }
    }
}
