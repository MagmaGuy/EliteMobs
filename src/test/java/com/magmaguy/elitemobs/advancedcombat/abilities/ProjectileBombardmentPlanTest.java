package com.magmaguy.elitemobs.advancedcombat.abilities;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectileBombardmentPlanTest {

    @Test
    void sixFourArrowVolleysBecomeTwentyFourVisibleDamageDeliveries() {
        ProjectileBombardmentPlan plan = ProjectileBombardmentPlan.create(
                7D, 80, 6, 4, 6, 42L);

        assertEquals(24, plan.impacts().size());
        assertEquals(24, plan.maximumDamageDeliveries());
        assertEquals(1.4D, plan.impactRadius(), 1.0E-9D);
        assertEquals(0, plan.impacts().get(0).launchTick());
        assertEquals(6, plan.impacts().get(0).impactTick());
        assertEquals(80, plan.impacts().get(23).impactTick());
        for (int volley = 0; volley < 6; volley++) {
            int volleyIndex = volley;
            List<ProjectileBombardmentPlan.Impact> impacts = plan.impacts().stream()
                    .filter(impact -> impact.volleyIndex() == volleyIndex)
                    .toList();
            assertEquals(4, impacts.size());
            assertEquals(1, impacts.stream().map(ProjectileBombardmentPlan.Impact::launchTick).distinct().count());
            assertEquals(1, impacts.stream().map(ProjectileBombardmentPlan.Impact::impactTick).distinct().count());
        }
        assertTrue(plan.impacts().stream().allMatch(impact ->
                Math.hypot(impact.offsetX(), impact.offsetZ()) <= 7D));
    }

    @Test
    void seededSpreadIsRepeatableWithoutMakingEveryCastIdentical() {
        ProjectileBombardmentPlan first = ProjectileBombardmentPlan.create(
                7D, 80, 6, 4, 6, 19L);
        ProjectileBombardmentPlan repeated = ProjectileBombardmentPlan.create(
                7D, 80, 6, 4, 6, 19L);
        ProjectileBombardmentPlan nextCast = ProjectileBombardmentPlan.create(
                7D, 80, 6, 4, 6, 20L);

        assertEquals(first, repeated);
        assertNotEquals(first.impacts().get(0), nextCast.impacts().get(0));
    }
}
