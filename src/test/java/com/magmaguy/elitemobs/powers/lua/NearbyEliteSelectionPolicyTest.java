package com.magmaguy.elitemobs.powers.lua;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NearbyEliteSelectionPolicyTest {

    private static final UUID WORLD = new UUID(1, 1);

    @Test
    void rejectsUnboundedQueries() {
        assertThrows(IllegalArgumentException.class,
                () -> NearbyEliteSelectionPolicy.select(WORLD, 0, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> NearbyEliteSelectionPolicy.select(WORLD, 65, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> NearbyEliteSelectionPolicy.select(WORLD, Double.NaN, List.of()));
    }

    @Test
    void filtersLifecycleWorldAndRadiusThenOrdersStably() {
        UUID nearB = new UUID(0, 20);
        UUID nearA = new UUID(0, 10);
        UUID dead = new UUID(0, 30);
        List<NearbyEliteSelectionPolicy.Candidate> candidates = List.of(
                candidate(nearB, WORLD, true, true, 4),
                candidate(nearA, WORLD, true, true, 4),
                candidate(dead, WORLD, true, false, 1),
                candidate(new UUID(0, 40), new UUID(2, 2), true, true, 1),
                candidate(new UUID(0, 50), WORLD, false, true, 1),
                candidate(new UUID(0, 60), WORLD, true, true, 101));

        assertEquals(List.of(nearA, nearB),
                NearbyEliteSelectionPolicy.select(WORLD, 10, candidates).stream()
                        .map(NearbyEliteSelectionPolicy.Candidate::entityId)
                        .toList());
    }

    @Test
    void hardCapsResultCount() {
        List<NearbyEliteSelectionPolicy.Candidate> candidates = new ArrayList<>();
        for (int index = 0; index < 40; index++) {
            candidates.add(candidate(new UUID(0, index + 1), WORLD, true, true, index));
        }

        assertEquals(32, NearbyEliteSelectionPolicy.select(WORLD, 64, candidates).size());
    }

    private static NearbyEliteSelectionPolicy.Candidate candidate(
            UUID id,
            UUID world,
            boolean tracked,
            boolean alive,
            double distanceSquared) {
        return new NearbyEliteSelectionPolicy.Candidate(
                id, world, tracked, alive, distanceSquared);
    }
}
