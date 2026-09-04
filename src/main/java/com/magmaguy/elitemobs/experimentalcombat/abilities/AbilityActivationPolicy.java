package com.magmaguy.elitemobs.experimentalcombat.abilities;

import java.util.Set;

/** Pure caster and range gates evaluated before an ability can spend resource. */
final class AbilityActivationPolicy {
    static final double LOW_HEALTH_ALLY_FRACTION = .35D;

    private AbilityActivationPolicy() {
    }

    static boolean permits(
            Set<AbilityMechanic> mechanics,
            boolean casterMoving,
            double nearestEnemyDistance,
            double minimumRange,
            double lowestAllyHealthFraction) {
        if (mechanics.contains(AbilityMechanic.REQUIRES_MOVEMENT) && !casterMoving) return false;
        if (mechanics.contains(AbilityMechanic.MINIMUM_RANGE)
                && nearestEnemyDistance < minimumRange) return false;
        return !mechanics.contains(AbilityMechanic.LOW_HEALTH_ALLY_ONLY)
                || lowestAllyHealthFraction <= LOW_HEALTH_ALLY_FRACTION;
    }

    static boolean permitsImpact(
            Set<AbilityMechanic> mechanics,
            double originToImpactDistance,
            double originToTargetDistance,
            double minimumRange) {
        if (!mechanics.contains(AbilityMechanic.MINIMUM_RANGE)) return true;
        return Double.isFinite(originToImpactDistance)
                && Double.isFinite(originToTargetDistance)
                && originToImpactDistance >= minimumRange
                && originToTargetDistance >= minimumRange;
    }
}
