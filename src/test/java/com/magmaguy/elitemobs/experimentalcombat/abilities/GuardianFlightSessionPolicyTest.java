package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuardianFlightSessionPolicyTest {

    @Test
    void arrivalWinsBeforeCollisionAndStallsBecomeObstructions() {
        assertEquals(
                GuardianFlightSessionPolicy.Decision.ARRIVED,
                evaluate(true, true, 4, 60, 1.5D, 20D, 2.1D, true, 3));
        assertEquals(
                GuardianFlightSessionPolicy.Decision.OBSTRUCTED,
                evaluate(true, true, 4, 60, 8D, 20D, 2.1D, false, 3));
    }

    @Test
    void invalidTargetsRangeAndLifetimeFailClosed() {
        assertEquals(
                GuardianFlightSessionPolicy.Decision.TARGET_INVALID,
                evaluate(true, false, 1, 60, 8D, 20D, 2.1D, false, 0));
        assertEquals(
                GuardianFlightSessionPolicy.Decision.OUT_OF_RANGE,
                evaluate(true, true, 1, 60, 21D, 20D, 2.1D, false, 0));
        assertEquals(
                GuardianFlightSessionPolicy.Decision.EXPIRED,
                evaluate(true, true, 60, 60, 8D, 20D, 2.1D, false, 0));
        assertEquals(
                GuardianFlightSessionPolicy.Decision.CONTINUE,
                evaluate(true, true, 1, 60, 8D, 20D, 2.1D, false, 0));
    }

    private static GuardianFlightSessionPolicy.Decision evaluate(
            boolean casterValid,
            boolean targetValid,
            int elapsedTicks,
            int maximumTicks,
            double targetDistance,
            double maximumTargetDistance,
            double arrivalDistance,
            boolean obstructed,
            int stalledTicks) {
        return GuardianFlightSessionPolicy.evaluate(new GuardianFlightSessionPolicy.Frame(
                casterValid,
                targetValid,
                elapsedTicks,
                maximumTicks,
                targetDistance,
                maximumTargetDistance,
                arrivalDistance,
                obstructed,
                stalledTicks));
    }
}
