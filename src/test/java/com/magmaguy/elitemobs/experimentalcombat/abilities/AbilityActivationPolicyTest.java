package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilityActivationPolicyTest {

    @Test
    void movementAndDistancePromisesFailBeforeResourceCanBeSpent() {
        assertFalse(AbilityActivationPolicy.permits(
                Set.of(AbilityMechanic.REQUIRES_MOVEMENT), false, 20D, 8D, 1D));
        assertFalse(AbilityActivationPolicy.permits(
                Set.of(AbilityMechanic.MINIMUM_RANGE), true, 7.99D, 8D, 1D));
        assertTrue(AbilityActivationPolicy.permits(
                Set.of(AbilityMechanic.REQUIRES_MOVEMENT, AbilityMechanic.MINIMUM_RANGE),
                true, 8D, 8D, 1D));
    }

    @Test
    void rescuePromiseRequiresAnActuallyDyingAlly() {
        Set<AbilityMechanic> mechanic = Set.of(AbilityMechanic.LOW_HEALTH_ALLY_ONLY);

        assertFalse(AbilityActivationPolicy.permits(mechanic, false, 0D, 0D, .36D));
        assertTrue(AbilityActivationPolicy.permits(mechanic, false, 0D, 0D, .35D));
    }

    @Test
    void delayedMinimumRangeChecksTravelPathAndActualTargetPosition() {
        Set<AbilityMechanic> mechanic = Set.of(AbilityMechanic.MINIMUM_RANGE);

        assertTrue(AbilityActivationPolicy.permitsImpact(mechanic, 10D, 9D, 8D));
        assertFalse(AbilityActivationPolicy.permitsImpact(mechanic, 7.99D, 9D, 8D));
        assertFalse(AbilityActivationPolicy.permitsImpact(mechanic, 10D, 7.99D, 8D));
    }
}
