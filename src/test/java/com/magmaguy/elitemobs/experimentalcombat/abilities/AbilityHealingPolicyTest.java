package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbilityHealingPolicyTest {

    private static final AbilityMechanicModifiers MODIFIERS = new AbilityMechanicModifiers(
            .8D, 1.2D, 1.15D, 1.25D, 1D, 1D, 1D, 1D);

    @Test
    void burstAndPeriodicHealingUseTheirOwnTypedMultipliers() {
        assertEquals(.8D, AbilityHealingPolicy.multiplier(
                MODIFIERS, AbilityHealingType.BURST, 1), 1.0E-9D);
        assertEquals(1.2D, AbilityHealingPolicy.multiplier(
                MODIFIERS, AbilityHealingType.PERIODIC, 1), 1.0E-9D);
    }

    @Test
    void groupedHealingRequiresTwoQualifiedEnemies() {
        assertEquals(.8D, AbilityHealingPolicy.multiplier(
                MODIFIERS, AbilityHealingType.BURST, 1), 1.0E-9D);
        assertEquals(.92D, AbilityHealingPolicy.multiplier(
                MODIFIERS, AbilityHealingType.BURST, 2), 1.0E-9D);
    }

    @Test
    void onlyPeriodicLifetimesUseTheDurationMultiplier() {
        assertEquals(100, AbilityHealingPolicy.durationTicks(
                100, AbilityHealingType.BURST, MODIFIERS));
        assertEquals(125, AbilityHealingPolicy.durationTicks(
                100, AbilityHealingType.PERIODIC, MODIFIERS));
    }
}
