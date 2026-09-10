package com.magmaguy.elitemobs.advancedcombat.minions;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorpseTargetSelectorTest {

    @Test
    void preciseSightLineWinsOverACloserOffAxisCorpse() {
        UUID precise = UUID.randomUUID();
        UUID offAxis = UUID.randomUUID();
        List<CorpseTargetSelector.Candidate> corpses = List.of(
                candidate(offAxis, 3, 0, 3),
                candidate(precise, 8, 0, .3));

        assertEquals(precise, CorpseTargetSelector.select(
                point(0, 0, 0), point(1, 0, 0), 12, corpses).orElseThrow());
    }

    @Test
    void forgivingForwardConeFindsAVisibleNearMiss() {
        UUID nearMiss = UUID.randomUUID();
        assertEquals(nearMiss, CorpseTargetSelector.select(
                point(0, 0, 0), point(1, 0, 0), 12,
                List.of(candidate(nearMiss, 7, 0, 3))).orElseThrow());
    }

    @Test
    void neverSelectsBehindOrBeyondRange() {
        assertTrue(CorpseTargetSelector.select(
                point(0, 0, 0), point(1, 0, 0), 10,
                List.of(
                        candidate(UUID.randomUUID(), -2, 0, 0),
                        candidate(UUID.randomUUID(), 11, 0, 0)))
                .isEmpty());
    }

    private static CorpseTargetSelector.Candidate candidate(UUID id, double x, double y, double z) {
        return new CorpseTargetSelector.Candidate(id, point(x, y, z));
    }

    private static CorpseTargetSelector.Point point(double x, double y, double z) {
        return new CorpseTargetSelector.Point(x, y, z);
    }
}
