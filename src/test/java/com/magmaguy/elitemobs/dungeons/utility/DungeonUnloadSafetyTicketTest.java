package com.magmaguy.elitemobs.dungeons.utility;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Protects the unload guard that prevents player-held instance worlds surviving cleanup. */
final class DungeonUnloadSafetyTicketTest {
    @Test void refusesWhenPlayerCannotBeMoved() {
        assertTrue(DungeonUtils.shouldRefuseUnload(true, true, false));
        assertTrue(DungeonUtils.shouldRefuseUnload(true, false, false));
    }
    @Test void permitsUnloadWhenNoPlayersOrTeleportSucceeded() {
        assertFalse(DungeonUtils.shouldRefuseUnload(false, false, false));
        assertFalse(DungeonUtils.shouldRefuseUnload(true, true, true));
    }
}
