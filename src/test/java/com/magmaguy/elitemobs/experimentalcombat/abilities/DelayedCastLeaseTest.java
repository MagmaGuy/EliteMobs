package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DelayedCastLeaseTest {
    @Test
    void onlyTheSameEligibleCasterLifecycleCanReachDelayedImpact() {
        UUID caster = UUID.randomUUID();
        DelayedCastLease lease = new DelayedCastLease(caster, 7L);

        assertTrue(lease.permits(caster, 7L, true));
        assertFalse(lease.permits(caster, 8L, true));
        assertFalse(lease.permits(UUID.randomUUID(), 7L, true));
        assertFalse(lease.permits(caster, 7L, false));
    }
}
