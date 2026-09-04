package com.magmaguy.elitemobs.mobconstructor;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElitePowerSuppressionTest {
    @Test
    void independentLeasesNotifyOnlyOnFirstAcquireAndLastRelease() {
        List<Boolean> transitions = new ArrayList<>();
        ElitePowerSuppression suppression = new ElitePowerSuppression(
                (reason, paused) -> transitions.add(paused));

        ElitePowerSuppression.Lease first = suppression.acquire(ElitePowerPauseReason.INTERRUPT);
        ElitePowerSuppression.Lease second = suppression.acquire(ElitePowerPauseReason.INTERRUPT);
        assertTrue(suppression.isSuppressed());
        assertEquals(List.of(true), transitions);

        first.close();
        first.close();
        assertTrue(suppression.isSuppressed());
        assertEquals(List.of(true), transitions);

        second.close();
        assertFalse(suppression.isSuppressed());
        assertEquals(List.of(true, false), transitions);
    }

    @Test
    void lifecycleCloseReleasesEveryActiveReasonExactlyOnce() {
        List<String> transitions = new ArrayList<>();
        ElitePowerSuppression suppression = new ElitePowerSuppression(
                (reason, paused) -> transitions.add(reason + ":" + paused));
        suppression.acquire(ElitePowerPauseReason.INTERRUPT);
        suppression.acquire(ElitePowerPauseReason.MIND_SERVICE);

        suppression.close();
        suppression.close();

        assertFalse(suppression.isSuppressed());
        assertEquals(List.of(
                "INTERRUPT:true",
                "MIND_SERVICE:true",
                "INTERRUPT:false",
                "MIND_SERVICE:false"), transitions);
    }
}
