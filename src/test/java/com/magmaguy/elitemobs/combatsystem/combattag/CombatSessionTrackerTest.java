package com.magmaguy.elitemobs.combatsystem.combattag;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatSessionTrackerTest {

    private static final long COMBAT_TIMEOUT_TICKS = 20L * 20L;

    @Test
    void firstDamageStartsCombatAndAdditionalDamageDoesNotRestartIt() {
        CombatSessionTracker tracker = new CombatSessionTracker(COMBAT_TIMEOUT_TICKS);
        UUID playerId = UUID.randomUUID();

        assertEquals(CombatSessionTracker.DamageResult.STARTED_COMBAT,
                tracker.recordDamage(playerId, 100L));
        assertTrue(tracker.isInCombat(playerId));
        assertEquals(CombatSessionTracker.DamageResult.REFRESHED_COMBAT,
                tracker.recordDamage(playerId, 200L));
        assertTrue(tracker.isInCombat(playerId));
    }

    @Test
    void combatExpiresExactlyAtTheFourHundredTickBoundary() {
        CombatSessionTracker tracker = new CombatSessionTracker(COMBAT_TIMEOUT_TICKS);
        UUID playerId = UUID.randomUUID();
        tracker.recordDamage(playerId, 100L);

        assertEquals(List.of(), tracker.expire(499L));
        assertTrue(tracker.isInCombat(playerId));

        assertEquals(List.of(playerId), tracker.expire(500L));
        assertFalse(tracker.isInCombat(playerId));
        assertEquals(List.of(), tracker.expire(500L));
    }

    @Test
    void laterDamageRefreshesTheFullCombatTimeout() {
        CombatSessionTracker tracker = new CombatSessionTracker(COMBAT_TIMEOUT_TICKS);
        UUID playerId = UUID.randomUUID();
        tracker.recordDamage(playerId, 100L);

        assertEquals(CombatSessionTracker.DamageResult.REFRESHED_COMBAT,
                tracker.recordDamage(playerId, 499L));
        assertEquals(List.of(), tracker.expire(500L));
        assertEquals(List.of(), tracker.expire(898L));
        assertEquals(List.of(playerId), tracker.expire(899L));
    }

    @Test
    void playersExpireIndependently() {
        CombatSessionTracker tracker = new CombatSessionTracker(COMBAT_TIMEOUT_TICKS);
        UUID firstPlayer = UUID.randomUUID();
        UUID secondPlayer = UUID.randomUUID();
        tracker.recordDamage(firstPlayer, 0L);
        tracker.recordDamage(secondPlayer, 100L);

        assertEquals(List.of(firstPlayer), tracker.expire(400L));
        assertFalse(tracker.isInCombat(firstPlayer));
        assertTrue(tracker.isInCombat(secondPlayer));

        assertEquals(List.of(secondPlayer), tracker.expire(500L));
        assertFalse(tracker.isInCombat(secondPlayer));
    }

}
