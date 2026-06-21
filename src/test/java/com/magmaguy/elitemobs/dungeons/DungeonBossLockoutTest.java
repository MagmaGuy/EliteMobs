package com.magmaguy.elitemobs.dungeons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DungeonBossLockoutTest {

    @Test
    void clearLockoutsRemovesEveryEntryAndReturnsCount() {
        DungeonBossLockout lockout = new DungeonBossLockout();
        lockout.addLockout("bossA:1,2,3", 60);
        lockout.addLockout("bossB:4,5,6", 60);

        int cleared = lockout.clearLockouts();

        assertEquals(2, cleared);
        assertTrue(lockout.getLockouts().isEmpty());
        assertFalse(lockout.isLockedOut("bossA:1,2,3"));
    }

    @Test
    void clearLockoutsOnEmptyMapReturnsZero() {
        DungeonBossLockout lockout = new DungeonBossLockout();
        assertEquals(0, lockout.clearLockouts());
    }

    @Test
    void removeLockoutRemovesOnlyTheTargetedEntry() {
        DungeonBossLockout lockout = new DungeonBossLockout();
        lockout.addLockout("bossA:1,2,3", 60);
        lockout.addLockout("bossB:4,5,6", 60);

        assertTrue(lockout.removeLockout("bossA:1,2,3"));
        assertFalse(lockout.isLockedOut("bossA:1,2,3"));
        assertTrue(lockout.isLockedOut("bossB:4,5,6"));
    }

    @Test
    void removeLockoutReturnsFalseWhenNoLockoutExists() {
        DungeonBossLockout lockout = new DungeonBossLockout();
        assertFalse(lockout.removeLockout("missing:0,0,0"));
    }
}
