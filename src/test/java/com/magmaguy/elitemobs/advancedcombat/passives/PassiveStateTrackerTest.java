package com.magmaguy.elitemobs.advancedcombat.passives;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PassiveStateTrackerTest {

    @Test
    void recentHitExpiresAndCanBeDiscardedWithoutWallClockSleep() {
        AtomicLong time = new AtomicLong(100L);
        PassiveStateTracker tracker = new PassiveStateTracker(time::get);
        UUID playerId = UUID.randomUUID();

        assertFalse(tracker.recentlyHit(playerId));
        tracker.recordHit(playerId);
        assertTrue(tracker.recentlyHit(playerId));

        time.addAndGet(PassiveStateTracker.RECENT_HIT_NANOS);
        assertFalse(tracker.recentlyHit(playerId));

        tracker.recordHit(playerId);
        tracker.clear(playerId);
        assertFalse(tracker.recentlyHit(playerId));
    }

    @Test
    void killChainAndBrokenWardStatesExpireIndependently() {
        AtomicLong time = new AtomicLong(500L);
        PassiveStateTracker tracker = new PassiveStateTracker(time::get);
        UUID playerId = UUID.randomUUID();

        tracker.recordEliteKill(playerId);
        tracker.recordWardBroken(playerId, 40);
        assertTrue(tracker.recentEliteKill(playerId));
        assertTrue(tracker.wardBroken(playerId));

        time.addAndGet(PassiveStateTracker.nanosForTicks(40));
        assertFalse(tracker.wardBroken(playerId));
        assertTrue(tracker.recentEliteKill(playerId));

        time.addAndGet(PassiveStateTracker.RECENT_ELITE_KILL_NANOS
                - PassiveStateTracker.nanosForTicks(40));
        assertFalse(tracker.recentEliteKill(playerId));
    }
}
