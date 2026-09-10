package com.magmaguy.elitemobs.advancedcombat;

import org.junit.jupiter.api.Test;

import static com.magmaguy.elitemobs.advancedcombat.AdvancedCombatShutdownPolicy.HealthCleanup.PRESERVE_FOR_SOFT_RELOAD;
import static com.magmaguy.elitemobs.advancedcombat.AdvancedCombatShutdownPolicy.HealthCleanup.RESTORE_ORDINARY_HEALTH;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AdvancedCombatShutdownPolicyTest {

    @Test
    void ordinaryShutdownRestoresOrdinaryHealth() {
        AdvancedCombatShutdownPolicy policy = new AdvancedCombatShutdownPolicy();

        assertEquals(RESTORE_ORDINARY_HEALTH, policy.consumeHealthCleanup());
    }

    @Test
    void softReloadPreservesHealthForExactlyOneShutdown() {
        AdvancedCombatShutdownPolicy policy = new AdvancedCombatShutdownPolicy();
        policy.prepareSoftReload();

        assertEquals(PRESERVE_FOR_SOFT_RELOAD, policy.consumeHealthCleanup());
        assertEquals(RESTORE_ORDINARY_HEALTH, policy.consumeHealthCleanup());
    }
}
