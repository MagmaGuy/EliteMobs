package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SourceSuppressionLeaseRegistryTest {
    @Test
    void rootsAreSourceOwnedAndExtensionCannotStealAnotherCastersLease() {
        SourceSuppressionLeaseRegistry<String, String> roots = new SourceSuppressionLeaseRegistry<>();
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
        roots.apply("elite", "second", 25L, FakeLease::new);
        roots.maintain(29L, ignored -> true);
        assertFalse(lease.closed);
        roots.maintain(30L, ignored -> true);
        assertTrue(lease.closed);
        assertTrue(roots.activeTargets().isEmpty());
        assertFalse(roots.ownedBy("elite", "first"));
    }

    @Test
    void removalAndCloseAlwaysReleaseSuppressionLease() {
        SourceSuppressionLeaseRegistry<String, String> roots = new SourceSuppressionLeaseRegistry<>();
        FakeLease removed = new FakeLease();
        FakeLease closed = new FakeLease(), invalid = new FakeLease();
        roots.apply("invalid", "caster", 50L, () -> invalid);
        roots.apply("removed", "caster", 50L, () -> removed);
        roots.apply("closed", "caster", 50L, () -> closed);

        roots.maintain(1L, target -> !target.equals("invalid"));
        assertTrue(invalid.closed);
        roots.remove("removed");
        roots.close();

        assertTrue(removed.closed);
        assertTrue(closed.closed);
    }

    @Test
    void clearingOneSourcePreservesOtherSourcesAndReleasesItsLastLease() {
        SourceSuppressionLeaseRegistry<String, String> roots = new SourceSuppressionLeaseRegistry<>();
        FakeLease shared = new FakeLease(), sole = new FakeLease();
        roots.apply("shared", "first", 50L, () -> shared);
        roots.apply("shared", "second", 60L, FakeLease::new);
        roots.apply("sole", "first", 50L, () -> sole);

        roots.clearSource("first");
        assertFalse(roots.ownedBy("shared", "first"));
        assertTrue(roots.ownedBy("shared", "second"));
        assertFalse(shared.closed);
        assertTrue(sole.closed);
        roots.clearSource("second");
        assertTrue(shared.closed);
        assertTrue(roots.activeTargets().isEmpty());
    }

    private static final class FakeLease implements AutoCloseable {
        private boolean closed;

        @Override
        public void close() {
            closed = true;
        }
    }
}
