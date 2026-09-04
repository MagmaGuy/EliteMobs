package com.magmaguy.elitemobs.experimentalcombat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExperimentalCombatRulesTest {

    @Test
    void vanillaHealthBecomesTheExperimentalBaseline() {
        assertEquals(50D, ExperimentalCombatRules.experimentalMaximumHealth(20D));
    }

    @Test
    void existingHealthBonusesRemainFlat() {
        assertEquals(68D, ExperimentalCombatRules.experimentalMaximumHealth(38D));
        assertEquals(148D, ExperimentalCombatRules.experimentalMaximumHealth(118D));
    }

    @Test
    void ordinaryMaximumCanBeRecoveredForExistingDamageRules() {
        assertEquals(20D, ExperimentalCombatRules.ordinaryMaximumHealth(50D));
        assertEquals(38D, ExperimentalCombatRules.ordinaryMaximumHealth(68D));
        assertEquals(118D, ExperimentalCombatRules.ordinaryMaximumHealth(148D));
    }
}
