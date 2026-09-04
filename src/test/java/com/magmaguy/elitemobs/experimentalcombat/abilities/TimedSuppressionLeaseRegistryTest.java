package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimedSuppressionLeaseRegistryTest {
    @Test
    void reapplicationExtendsWithoutOpeningASecondActorLease() {
        TimedSuppressionLeaseRegistry<String> registry = new TimedSuppressionLeaseRegistry<>();
        AtomicInteger opens = new AtomicInteger();
        FakeLease lease = new FakeLease();

        assertTrue(registry.apply("elite", 40L, () -> {
            opens.incrementAndGet();
            return lease;
        }));
        assertTrue(registry.apply("elite", 25L, () -> {
            opens.incrementAndGet();
            return new FakeLease();
        }));
        assertEquals(1, opens.get());

        registry.maintain(39L, ignored -> true);
        assertFalse(lease.closed);
        registry.maintain(40L, ignored -> true);
        assertTrue(lease.closed);
        assertEquals(1, lease.closeCalls);
    }

    @Test
    void invalidTargetAndRegistryCloseReleaseLeases() {
        TimedSuppressionLeaseRegistry<String> registry = new TimedSuppressionLeaseRegistry<>();
        FakeLease invalid = new FakeLease();
        FakeLease remaining = new FakeLease();
        registry.apply("invalid", 100L, () -> invalid);
        registry.apply("remaining", 100L, () -> remaining);

        registry.maintain(1L, target -> !target.equals("invalid"));
        registry.close();

        assertTrue(invalid.closed);
        assertTrue(remaining.closed);
        assertEquals(0, registry.size());
    }

    private static final class FakeLease implements AutoCloseable {
        private boolean closed;
        private int closeCalls;

        @Override
        public void close() {
            if (closed) return;
            closed = true;
            closeCalls++;
        }
    }
}
