package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RootLeaseRegistryTest {
    @Test
    void rootsAreSourceOwnedAndExtensionCannotStealAnotherCastersLease() {
        RootLeaseRegistry<String, String> roots = new RootLeaseRegistry<>();
        FakeLease lease = new FakeLease();
        AtomicInteger opens = new AtomicInteger();

        assertTrue(roots.apply("elite", "first", 20L, () -> {
            opens.incrementAndGet();
            return lease;
        }));
        assertTrue(roots.apply("elite", "second", 30L, () -> {
            opens.incrementAndGet();
            return new FakeLease();
        }));
        assertEquals(1, opens.get());
        assertTrue(roots.ownedBy("elite", "first"));
        assertTrue(roots.ownedBy("elite", "second"));
        assertFalse(roots.ownedBy("elite", "missing"));
        assertFalse(roots.extend("elite", "missing", 10L, 5L));
        assertTrue(roots.extend("elite", "first", 10L, 5L));

        roots.maintain(21L, ignored -> true);
        assertTrue(roots.activeTargets().contains("elite"));
        roots.maintain(30L, ignored -> true);
        assertTrue(lease.closed);
        assertTrue(roots.activeTargets().isEmpty());
        assertFalse(roots.ownedBy("elite", "first"));
    }

    @Test
    void removalAndCloseAlwaysReleaseSuppressionLease() {
        RootLeaseRegistry<String, String> roots = new RootLeaseRegistry<>();
        FakeLease removed = new FakeLease();
        FakeLease closed = new FakeLease();
        roots.apply("removed", "caster", 50L, () -> removed);
        roots.apply("closed", "caster", 50L, () -> closed);

        roots.remove("removed");
        roots.close();

        assertTrue(removed.closed);
        assertTrue(closed.closed);
    }

    private static final class FakeLease implements AutoCloseable {
        private boolean closed;

        @Override
        public void close() {
            closed = true;
        }
    }
}
