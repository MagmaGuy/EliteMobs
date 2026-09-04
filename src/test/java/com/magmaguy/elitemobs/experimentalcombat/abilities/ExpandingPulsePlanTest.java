package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpandingPulsePlanTest {

    @Test
    void threePulsesReachDisjointRingsAcrossThePromisedDuration() {
        ExpandingPulsePlan plan = ExpandingPulsePlan.create(8D, 3, 60);

        assertEquals(List.of(
                        new ExpandingPulsePlan.Pulse(0, 0, 0D, 8D / 3D),
                        new ExpandingPulsePlan.Pulse(1, 30, 8D / 3D, 16D / 3D),
                        new ExpandingPulsePlan.Pulse(2, 60, 16D / 3D, 8D)),
                plan.pulses());
        assertEquals(0, plan.pulseForDistance(2D).orElseThrow().index());
        assertEquals(1, plan.pulseForDistance(4D).orElseThrow().index());
        assertEquals(2, plan.pulseForDistance(7D).orElseThrow().index());
        assertTrue(plan.pulseForDistance(8.01D).isEmpty());
    }
}
