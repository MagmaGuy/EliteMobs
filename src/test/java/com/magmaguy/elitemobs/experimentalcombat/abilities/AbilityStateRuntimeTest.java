package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilityStateRuntimeTest {
    @Test
    void deathGuardFailsClosedForInvalidState() {
        assertEquals(0D, AbilityStateMath.deathGuardDamageCeiling(Double.NaN, 8D));
        assertEquals(0D, AbilityStateMath.deathGuardDamageCeiling(20D, Double.POSITIVE_INFINITY));
    }

    @Test
    void retaliationHealingComesFromTheStoredHitBudget() {
        assertEquals(18D, AbilityStateMath.retaliationHealing(40D, .45D));
        assertEquals(0D, AbilityStateMath.retaliationHealing(Double.NaN, .45D));
    }

    @Test
    void executeMarkGetsStrongerAsTargetHealthFalls() {
        assertEquals(1.18D, AbilityStateMath.healthScaledMark(1.18D, 1D), 1.0E-9D);
        assertEquals(1.45D, AbilityStateMath.healthScaledMark(1.18D, 0D), 1.0E-9D);
    }

    @Test
    void ownedBarrierBreakAndMajorThreatAreIndependentTriggers() {
        assertFalse(AbilityStateMath.breaksOwnedBarrier(7.99D, 8D));
        assertTrue(AbilityStateMath.breaksOwnedBarrier(8D, 8D));
        assertFalse(AbilityStateMath.isMajorThreat(14.99D, 100D, 100D));
        assertTrue(AbilityStateMath.isMajorThreat(15D, 100D, 100D));
        assertTrue(AbilityStateMath.isMajorThreat(8D, 7D, 100D));
    }
}
