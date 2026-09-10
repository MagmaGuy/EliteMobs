package com.magmaguy.elitemobs.advancedcombat.constructs;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstructLifecycleTrackerTest {
    @Test
    void closesEachOwnedConstructExactlyOnceWithItsAuthoredIdentity() {
        ConstructLifecycleTracker tracker = new ConstructLifecycleTracker();
        UUID constructId = UUID.randomUUID();
        UUID casterId = UUID.randomUUID();
        ConstructLifecycleTracker.Lease opened = tracker.open(
                constructId, casterId, "aegis.utility", 100,
                ClassConstructVisualRegistry.AnchorMode.FIXED);

        assertEquals(1, tracker.size());
        assertEquals(opened, tracker.close(constructId).orElseThrow());
        assertTrue(tracker.close(constructId).isEmpty());
        assertEquals(0, tracker.size());
    }
}
