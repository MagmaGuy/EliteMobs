package com.magmaguy.elitemobs.experimentalcombat.minions;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbyssalGateFootprintPlannerTest {

    @Test
    void createsAThreeByTwoPortalSurfaceAboveSolidGround() {
        Set<GateBlockPosition> ground = Set.of(
                new GateBlockPosition(0, 63, 0),
                new GateBlockPosition(1, 63, 0));
        AbyssalGateFootprint footprint = AbyssalGateFootprintPlanner.find(
                new GateBlockPosition(0, 64, 0), GateAxis.X,
                probe(ground, Set.of(), Set.of())).orElseThrow();

        assertEquals(6, footprint.blocks().size());
        assertEquals(Set.of(
                new GateBlockPosition(0, 64, 0), new GateBlockPosition(1, 64, 0),
                new GateBlockPosition(0, 65, 0), new GateBlockPosition(1, 65, 0),
                new GateBlockPosition(0, 66, 0), new GateBlockPosition(1, 66, 0)),
                new HashSet<>(footprint.blocks()));
    }

    @Test
    void neverPlansAcrossNonAirBlocksButAcceptsAlreadyOwnedPortalBlocksForOverlap() {
        Set<GateBlockPosition> ground = Set.of(
                new GateBlockPosition(0, 63, 0),
                new GateBlockPosition(1, 63, 0));
        GateBlockPosition overlap = new GateBlockPosition(0, 65, 0);
        GateBlockPosition obstruction = new GateBlockPosition(1, 65, 0);

        assertTrue(AbyssalGateFootprintPlanner.find(
                new GateBlockPosition(0, 64, 0), GateAxis.X,
                probe(ground, Set.of(overlap), Set.of(obstruction))).isEmpty());
        assertTrue(AbyssalGateFootprintPlanner.find(
                new GateBlockPosition(0, 64, 0), GateAxis.X,
                probe(ground, Set.of(overlap), Set.of())).isPresent());
    }

    @Test
    void refusesAnUnloadedFootprintWithoutProbingOrForcingAnotherChunk() {
        Set<GateBlockPosition> ground = Set.of(
                new GateBlockPosition(15, 63, 0),
                new GateBlockPosition(16, 63, 0));
        assertTrue(AbyssalGateFootprintPlanner.find(
                new GateBlockPosition(15, 64, 0), GateAxis.X,
                probe(ground, Set.of(), Set.of(), position -> position.x() < 16)).isEmpty());
    }

    private static AbyssalGateFootprintPlanner.Probe probe(
            Set<GateBlockPosition> solid,
            Set<GateBlockPosition> ownedPortal,
            Set<GateBlockPosition> obstructed) {
        return probe(solid, ownedPortal, obstructed, ignored -> true);
    }

    private static AbyssalGateFootprintPlanner.Probe probe(
            Set<GateBlockPosition> solid,
            Set<GateBlockPosition> ownedPortal,
            Set<GateBlockPosition> obstructed,
            java.util.function.Predicate<GateBlockPosition> loaded) {
        return new AbyssalGateFootprintPlanner.Probe() {
            @Override
            public boolean loaded(GateBlockPosition position) {
                return loaded.test(position);
            }

            @Override
            public boolean solid(GateBlockPosition position) {
                return solid.contains(position);
            }

            @Override
            public boolean available(GateBlockPosition position) {
                return ownedPortal.contains(position)
                        || (!solid.contains(position) && !obstructed.contains(position));
            }
        };
    }
}
