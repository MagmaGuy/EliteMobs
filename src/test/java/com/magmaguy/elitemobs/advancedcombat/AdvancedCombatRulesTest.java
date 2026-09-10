package com.magmaguy.elitemobs.advancedcombat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdvancedCombatRulesTest {

    @Test
    void vanillaHealthBecomesTheAdvancedBaseline() {
        assertEquals(50D, AdvancedCombatRules.advancedMaximumHealth(20D));
    }

    @Test
    void existingHealthBonusesRemainFlat() {
        assertEquals(68D, AdvancedCombatRules.advancedMaximumHealth(38D));
        assertEquals(148D, AdvancedCombatRules.advancedMaximumHealth(118D));
    }

    @Test
    void ordinaryMaximumCanBeRecoveredForExistingDamageRules() {
        assertEquals(20D, AdvancedCombatRules.ordinaryMaximumHealth(50D));
        assertEquals(38D, AdvancedCombatRules.ordinaryMaximumHealth(68D));
        assertEquals(118D, AdvancedCombatRules.ordinaryMaximumHealth(148D));
    }
}
