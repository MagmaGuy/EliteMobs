package com.magmaguy.elitemobs.experimentalcombat;

import org.junit.jupiter.api.Test;

import static com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatShutdownPolicy.HealthCleanup.PRESERVE_FOR_SOFT_RELOAD;
import static com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatShutdownPolicy.HealthCleanup.RESTORE_ORDINARY_HEALTH;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExperimentalCombatShutdownPolicyTest {

    @Test
    void ordinaryShutdownRestoresOrdinaryHealth() {
        ExperimentalCombatShutdownPolicy policy = new ExperimentalCombatShutdownPolicy();

        assertEquals(RESTORE_ORDINARY_HEALTH, policy.consumeHealthCleanup());
    }

    @Test
    void softReloadPreservesHealthForExactlyOneShutdown() {
        ExperimentalCombatShutdownPolicy policy = new ExperimentalCombatShutdownPolicy();
        policy.prepareSoftReload();

        assertEquals(PRESERVE_FOR_SOFT_RELOAD, policy.consumeHealthCleanup());
        assertEquals(RESTORE_ORDINARY_HEALTH, policy.consumeHealthCleanup());
    }
}
