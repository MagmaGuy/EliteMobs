package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilityLevelScalingTest {

    @Test
    void levelOneUsesTheFirstBenefitStep() {
        assertEquals(1.0025D, AbilityLevelScaling.multiplier(1), 1.0E-9D);
    }

    @Test
    void benefitsKeepGrowingPastTheXpSoftCap() {
        assertEquals(1.25D, AbilityLevelScaling.multiplier(100), 1.0E-9D);
        assertEquals(1.2525D, AbilityLevelScaling.multiplier(101), 1.0E-9D);
        assertTrue(AbilityLevelScaling.multiplier(150) > AbilityLevelScaling.multiplier(101));
    }
}
