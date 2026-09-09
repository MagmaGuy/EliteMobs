package com.magmaguy.elitemobs.mobconstructor.custombosses;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnLifecycleTest {

    @Test
    void restorationStartsTrackingAndEscapeWithoutSendingAnAnnouncement() {
        InvocationCounts counts = apply(SpawnLifecycle.Context.RESTORED);

        assertEquals(1, counts.tracking.get());
        assertEquals(0, counts.announcements.get());
        assertEquals(1, counts.escapeTasks.get());
    }

    @Test
    void genuineSpawnsAndRespawnsKeepAllSpawnEffects() {
        InvocationCounts counts = apply(SpawnLifecycle.Context.ANNOUNCED);

        assertEquals(1, counts.tracking.get());
        assertEquals(1, counts.announcements.get());
        assertEquals(1, counts.escapeTasks.get());
    }

    @Test
    void silentInternalSpawnsKeepAllAnnouncementLifecycleEffectsDisabled() {
        InvocationCounts counts = apply(SpawnLifecycle.Context.SILENT);

        assertEquals(0, counts.tracking.get());
        assertEquals(0, counts.announcements.get());
        assertEquals(0, counts.escapeTasks.get());
    }

    @Test
    void onlyAnActivePersistedBossUsesRestorationSemantics() {
        assertEquals(SpawnLifecycle.Context.RESTORED,
                RegionalBossSpawnPolicy.forPersistedState(0));
        assertEquals(SpawnLifecycle.Context.ANNOUNCED,
                RegionalBossSpawnPolicy.forPersistedState(1_700_000_000_000L));
    }

    @Test
    void restoredReloadsKeepBossTrackableWithoutSendingNewPrompts() {
        TrackingFixture fixture = new TrackingFixture();

        fixture.apply(SpawnLifecycle.Context.ANNOUNCED);
        assertAll(
                () -> assertTrue(fixture.isTrackable()),
                () -> assertEquals(1, fixture.prompts.get()));

        fixture.unload();
        assertFalse(fixture.isTrackable());
        fixture.apply(SpawnLifecycle.Context.RESTORED);
        assertAll(
                () -> assertTrue(fixture.isTrackable()),
                () -> assertEquals(1, fixture.prompts.get()));

        fixture.unload();
        assertFalse(fixture.isTrackable());
        fixture.apply(SpawnLifecycle.Context.RESTORED);
        assertAll(
                () -> assertTrue(fixture.isTrackable()),
                () -> assertEquals(1, fixture.prompts.get()));
    }

    private static InvocationCounts apply(SpawnLifecycle.Context context) {
        InvocationCounts counts = new InvocationCounts();
        SpawnLifecycle.apply(
                context,
                ignored -> counts.tracking.incrementAndGet(),
                counts.announcements::incrementAndGet,
                counts.escapeTasks::incrementAndGet);
        return counts;
    }

    private static final class InvocationCounts {
        private final AtomicInteger tracking = new AtomicInteger();
        private final AtomicInteger announcements = new AtomicInteger();
        private final AtomicInteger escapeTasks = new AtomicInteger();
    }

    private static final class TrackingFixture {
        private static final String BOSS = "boss";
        private final Set<String> trackableBosses = new HashSet<>();
        private final AtomicInteger prompts = new AtomicInteger();
        private final BossTrackingLifecycle<TrackingHandle> lifecycle = new BossTrackingLifecycle<>(
                () -> trackableBosses.add(BOSS),
                () -> trackableBosses.remove(BOSS),
                () -> new TrackingHandle(prompts));

        private void apply(SpawnLifecycle.Context context) {
            SpawnLifecycle.apply(context, lifecycle::activate, () -> {
            }, () -> {
            });
        }

        private void unload() {
            lifecycle.deactivate();
        }

        private boolean isTrackable() {
            return trackableBosses.contains(BOSS);
        }
    }

    private static final class TrackingHandle implements BossTrackingLifecycle.Handle {
        private final AtomicInteger prompts;

        private TrackingHandle(AtomicInteger prompts) {
            this.prompts = prompts;
        }

        @Override
        public void dispose() {
        }

        @Override
        public void notifyPlayers() {
            prompts.incrementAndGet();
        }
    }
}
