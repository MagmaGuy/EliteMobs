package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.api.power.ElitePowerActionResult;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EliteLuaPowerServiceMindActionGateTest {
    @Test
    void actorSuppressionRejectsBeforeAnyAttachedDispatcherRuns() {
        EliteEntity actor = new EliteEntity();
        AtomicInteger downstreamCalls = new AtomicInteger();

        ElitePowerSuppression.Lease interrupt =
                actor.getPowerSuppression().acquire(ElitePowerPauseReason.INTERRUPT);
        assertEquals(ElitePowerActionResult.REJECTED,
                EliteLuaPowerServiceImpl.dispatchIfPowerActive(actor, () -> {
                    downstreamCalls.incrementAndGet();
                    return ElitePowerActionResult.ACCEPTED;
                }));
        assertEquals(0, downstreamCalls.get());

        interrupt.close();
        assertEquals(ElitePowerActionResult.ACCEPTED,
                EliteLuaPowerServiceImpl.dispatchIfPowerActive(actor, () -> {
                    downstreamCalls.incrementAndGet();
                    return ElitePowerActionResult.ACCEPTED;
                }));
        assertEquals(1, downstreamCalls.get());
    }
}
