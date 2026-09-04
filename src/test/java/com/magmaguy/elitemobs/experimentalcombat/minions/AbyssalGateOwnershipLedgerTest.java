package com.magmaguy.elitemobs.experimentalcombat.minions;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbyssalGateOwnershipLedgerTest {

    @Test
    void overlappingGatesReferenceCountSharedPortalBlocks() {
        AbyssalGateOwnershipLedger ledger = new AbyssalGateOwnershipLedger();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        GateBlockPosition shared = new GateBlockPosition(1, 65, 1);
        GateBlockPosition firstOnly = new GateBlockPosition(0, 65, 1);
        GateBlockPosition secondOnly = new GateBlockPosition(2, 65, 1);
        ledger.reserve(first, List.of(firstOnly, shared));
        ledger.reserve(second, List.of(shared, secondOnly));

        assertEquals(Set.of(firstOnly), ledger.release(first));
        assertTrue(ledger.isOwned(shared));
        assertEquals(Set.of(shared, secondOnly), ledger.release(second));
        assertTrue(ledger.isEmpty());
    }

    @Test
    void releasingAnUnknownOrAlreadyReleasedCastCannotClaimBlocks() {
        AbyssalGateOwnershipLedger ledger = new AbyssalGateOwnershipLedger();
        UUID gate = UUID.randomUUID();
        GateBlockPosition block = new GateBlockPosition(0, 64, 0);
        ledger.reserve(gate, List.of(block));
        assertEquals(Set.of(block), ledger.release(gate));
        assertTrue(ledger.release(gate).isEmpty());
        assertTrue(ledger.release(UUID.randomUUID()).isEmpty());
    }

    @Test
    void oneCastCannotBeReservedTwice() {
        AbyssalGateOwnershipLedger ledger = new AbyssalGateOwnershipLedger();
        UUID gate = UUID.randomUUID();
        ledger.reserve(gate, List.of(new GateBlockPosition(0, 64, 0)));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> ledger.reserve(gate, List.of(new GateBlockPosition(1, 64, 0))));
    }
}
