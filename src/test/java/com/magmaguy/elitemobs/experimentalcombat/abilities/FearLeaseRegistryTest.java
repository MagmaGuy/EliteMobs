package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FearLeaseRegistryTest {
    @Test
    void reapplicationRedirectsTheExistingLeaseAndOnlyExtendsItsLifetime() {
        FearLeaseRegistry<String, String> registry = new FearLeaseRegistry<>();
        UUID firstCaster = UUID.randomUUID();
        UUID secondCaster = UUID.randomUUID();
        FakeHandle handle = new FakeHandle();
        AtomicInteger opens = new AtomicInteger();

        assertTrue(registry.apply("elite", firstCaster, "first", 40L, () -> {
            opens.incrementAndGet();
            return Optional.of(handle);
        }));
        assertTrue(registry.apply("elite", secondCaster, "second", 30L, () -> {
            opens.incrementAndGet();
            return Optional.of(new FakeHandle());
        }));

        assertEquals(1, opens.get());
        assertEquals("second", handle.lastSource);
        assertFalse(registry.ownedBy("elite", firstCaster));
        assertTrue(registry.ownedBy("elite", secondCaster));

        registry.maintain(31L, ignored -> true,
                source -> Optional.of(source.equals(secondCaster) ? "live-second" : "wrong"));
        assertFalse(handle.closed);
        assertEquals("live-second", handle.lastSource);

        registry.maintain(40L, ignored -> true, ignored -> Optional.of("still-live"));
        assertTrue(handle.closed);
        assertEquals(1, handle.closeCalls);
        assertFalse(registry.ownedBy("elite", secondCaster));
    }

    @Test
    void invalidTargetOrMissingSourceClosesExactlyOnce() {
        FearLeaseRegistry<String, String> registry = new FearLeaseRegistry<>();
        FakeHandle invalidTarget = new FakeHandle();
        FakeHandle missingSource = new FakeHandle();

        registry.apply("invalid", UUID.randomUUID(), "source", 100L,
                () -> Optional.of(invalidTarget));
        registry.apply("missing", UUID.randomUUID(), "source", 100L,
                () -> Optional.of(missingSource));

        registry.maintain(1L,
                target -> !target.equals("invalid"),
                ignored -> Optional.empty());
        registry.close();

        assertEquals(1, invalidTarget.closeCalls);
        assertEquals(1, missingSource.closeCalls);
        assertEquals(0, registry.size());
    }

    @Test
    void unsupportedMovementFailsClosedWithoutInstallingARecord() {
        FearLeaseRegistry<String, String> registry = new FearLeaseRegistry<>();

        assertFalse(registry.apply(
                "elite", UUID.randomUUID(), "source", 40L, Optional::empty));
        assertEquals(0, registry.size());
    }

    @Test
    void failedRuntimeRetargetIsContainedAndTheLeaseIsClosed() {
        FearLeaseRegistry<String, String> registry = new FearLeaseRegistry<>();
        FakeHandle handle = new FakeHandle();
        registry.apply("elite", UUID.randomUUID(), "source", 100L,
                () -> Optional.of(handle));
        handle.throwOnRetarget = true;

        assertDoesNotThrow(() -> registry.maintain(
                1L, ignored -> true, ignored -> Optional.of("moved")));

        assertTrue(handle.closed);
        assertEquals(0, registry.size());
    }

    @Test
    void deactivationClosesOnlyTheCurrentOwnersFear() {
        FearLeaseRegistry<String, String> registry = new FearLeaseRegistry<>();
        UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        FakeHandle owned = new FakeHandle(), transferred = new FakeHandle();
        registry.apply("owned", first, "first", 100L, () -> Optional.of(owned));
        registry.apply("transferred", first, "first", 100L, () -> Optional.of(transferred));
        registry.apply("transferred", second, "second", 100L, Optional::empty);

        registry.clearSource(first);
        assertEquals(1, owned.closeCalls);
        assertFalse(transferred.closed);
        assertTrue(registry.ownedBy("transferred", second));
        registry.clearSource(second);
        assertEquals(1, transferred.closeCalls);
        assertEquals(0, registry.size());
    }

    private static final class FakeHandle implements FearLeaseRegistry.Redirectable<String> {
        private String lastSource;
        private boolean closed;
        private int closeCalls;
        private boolean throwOnRetarget;

        @Override
        public boolean isActive() {
            return !closed;
        }

        @Override
        public boolean retarget(String source) {
            if (throwOnRetarget) throw new IllegalStateException("unsupported after activation");
            if (closed) return false;
            lastSource = source;
            return true;
        }

        @Override
        public void close() {
            if (closed) return;
            closed = true;
            closeCalls++;
        }
    }
}
