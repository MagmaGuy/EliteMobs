package com.magmaguy.elitemobs.advancedcombat.abilities;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PiercingLinePolicyTest {

    @Test
    void returnsEveryIntersectedTargetInImpactOrder() {
        List<String> targets = PiercingLinePolicy.intersections(
                        new PiercingLinePolicy.Point(0D, 1D, 0D),
                        new PiercingLinePolicy.Point(1D, 0D, 0D),
                        20D,
                        List.of(
                                candidate("far", 11D, 13D, 0D, 2D),
                                candidate("near", 3D, 5D, 0D, 2D),
                                candidate("middle", 7D, 9D, 0D, 2D)))
                .stream()
                .map(PiercingLinePolicy.Intersection::target)
                .toList();

        assertEquals(List.of("near", "middle", "far"), targets);
    }

    @Test
    void excludesTargetsOffTheLineBehindTheCasterOrPastTerrain() {
        List<String> targets = PiercingLinePolicy.intersections(
                        new PiercingLinePolicy.Point(0D, 1D, 0D),
                        new PiercingLinePolicy.Point(1D, 0D, 0D),
                        10D,
                        List.of(
                                candidate("valid", 4D, 6D, 0D, 2D),
                                candidate("off-line", 4D, 6D, 4D, 6D),
                                candidate("behind", -4D, -2D, 0D, 2D),
                                candidate("blocked", 12D, 14D, 0D, 2D)))
                .stream()
                .map(PiercingLinePolicy.Intersection::target)
                .toList();

        assertEquals(List.of("valid"), targets);
    }

    private static PiercingLinePolicy.Candidate<String> candidate(
            String id, double minX, double maxX, double minZ, double maxZ) {
        return new PiercingLinePolicy.Candidate<>(id,
                new PiercingLinePolicy.Box(minX, 0D, minZ, maxX, 3D, maxZ));
    }
}
