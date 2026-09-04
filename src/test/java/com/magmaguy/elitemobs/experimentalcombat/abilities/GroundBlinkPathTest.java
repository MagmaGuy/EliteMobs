package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroundBlinkPathTest {

    @Test
    void flatGroundReachesTheSixBlockMaximum() {
        GroundBlinkPath.Destination destination = GroundBlinkPath.scan(
                        new Vector(2D, 64D, -3D),
                        new Vector(1D, .8D, 0D),
                        6D,
                        (x, z, previousY) -> OptionalDouble.of(64D))
                .orElseThrow();

        assertEquals(8D, destination.x(), 1.0E-9D);
        assertEquals(64D, destination.y(), 1.0E-9D);
        assertEquals(-3D, destination.z(), 1.0E-9D);
        assertEquals(6D, destination.distance(), 1.0E-9D);
    }

    @Test
    void obstructionStopsAtTheLastValidGroundPoint() {
        GroundBlinkPath.Destination destination = GroundBlinkPath.scan(
                        new Vector(0D, 70D, 0D),
                        new Vector(1D, 0D, 0D),
                        6D,
                        (x, z, previousY) -> x < 3D
                                ? OptionalDouble.of(70D)
                                : OptionalDouble.empty())
                .orElseThrow();

        assertEquals(2.75D, destination.x(), 1.0E-9D);
        assertEquals(2.75D, destination.distance(), 1.0E-9D);
    }

    @Test
    void oneBlockStepsPassButLargerCliffsStopBeforeTheEdge() {
        GroundBlinkPath.Destination destination = GroundBlinkPath.scan(
                        new Vector(0D, 64D, 0D),
                        new Vector(1D, 0D, 0D),
                        6D,
                        (x, z, previousY) -> {
                            if (x < 2D) return OptionalDouble.of(64D);
                            if (x < 4D) return OptionalDouble.of(65D);
                            return OptionalDouble.of(63D);
                        })
                .orElseThrow();

        assertEquals(3.75D, destination.x(), 1.0E-9D);
        assertEquals(65D, destination.y(), 1.0E-9D);
    }

    @Test
    void verticalAimHasNoGroundDirection() {
        assertTrue(GroundBlinkPath.scan(
                new Vector(0D, 64D, 0D),
                new Vector(0D, 1D, 0D),
                6D,
                (x, z, previousY) -> OptionalDouble.of(previousY)).isEmpty());
    }
}
