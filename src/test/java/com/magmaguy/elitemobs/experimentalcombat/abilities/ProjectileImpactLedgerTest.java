package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectileImpactLedgerTest {

    @Test
    void nativeAndModeledRoutesCanOnlyClaimOneImpact() {
        ProjectileImpactLedger<String> ledger = new ProjectileImpactLedger<>();
        UUID projectileId = UUID.randomUUID();
        UUID casterId = UUID.randomUUID();

        assertTrue(ledger.register(projectileId, casterId, "arcane-bolt"));
        assertEquals("arcane-bolt", ledger.claim(projectileId).orElseThrow());
        assertTrue(ledger.claim(projectileId).isEmpty());
    }

    @Test
    void aBlockCollisionRetiresTheCarrierWithoutAnImpact() {
        ProjectileImpactLedger<String> ledger = new ProjectileImpactLedger<>();
        UUID projectileId = UUID.randomUUID();
        UUID casterId = UUID.randomUUID();

        assertTrue(ledger.register(projectileId, casterId, "arcane-bolt"));
        assertTrue(ledger.retire(projectileId).isPresent());
        assertTrue(ledger.claim(projectileId).isEmpty());
    }

    @Test
    void casterDeactivationRetiresEveryOwnedCarrier() {
        ProjectileImpactLedger<String> ledger = new ProjectileImpactLedger<>();
        UUID firstCaster = UUID.randomUUID();
        UUID secondCaster = UUID.randomUUID();
        UUID firstProjectile = UUID.randomUUID();
        UUID secondProjectile = UUID.randomUUID();

        assertTrue(ledger.register(firstProjectile, firstCaster, "first"));
        assertTrue(ledger.register(secondProjectile, secondCaster, "second"));

        assertEquals(1, ledger.retireCaster(firstCaster).size());
        assertFalse(ledger.contains(firstProjectile));
        assertTrue(ledger.contains(secondProjectile));
    }
}
