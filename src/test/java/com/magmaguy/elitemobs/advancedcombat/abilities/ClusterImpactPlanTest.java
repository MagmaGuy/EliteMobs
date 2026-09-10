package com.magmaguy.elitemobs.advancedcombat.abilities;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClusterImpactPlanTest {

    @Test
    void onePrimaryBlastScattersSmallerDelayedBlastsInsideTheAuthoredArea() {
        List<ClusterImpactPlan.Impact> impacts = ClusterImpactPlan.create(6D, 3, 0D);

        assertEquals(3, impacts.size());
        assertEquals(0D, impacts.get(0).offsetX(), 1.0E-9D);
        assertEquals(0D, impacts.get(0).offsetZ(), 1.0E-9D);
        assertEquals(0, impacts.get(0).delayTicks());
        assertTrue(impacts.get(1).delayTicks() > impacts.get(0).delayTicks());
        assertTrue(impacts.get(2).delayTicks() > impacts.get(1).delayTicks());
        assertTrue(impacts.stream().allMatch(impact ->
                Math.hypot(impact.offsetX(), impact.offsetZ()) + impact.blastRadius() <= 6D + 1.0E-9D));
        assertTrue(impacts.stream().map(impact -> impact.offsetX() + ":" + impact.offsetZ())
                .distinct().count() == 3L);
    }
}
