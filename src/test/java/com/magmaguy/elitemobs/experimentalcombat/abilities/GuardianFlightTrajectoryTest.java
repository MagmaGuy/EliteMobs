package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardianFlightTrajectoryTest {

    @Test
    void steeringAcceleratesTowardTheLiveTargetWithoutTeleporting() {
        Vector first = GuardianFlightTrajectory.steer(
                new Vector(), new Vector(10D, 4D, 0D));
        Vector second = GuardianFlightTrajectory.steer(
                first, new Vector(8D, 1D, 3D));

        assertEquals(.22D, first.length(), 1.0E-9D);
        assertTrue(first.getX() > 0D);
        assertTrue(first.getY() > 0D);
        assertTrue(second.getZ() > 0D);
        assertTrue(second.length() > first.length());
        assertTrue(second.length() <= 1.3D);
    }

    @Test
    void cancelMomentumScalesWithTravelAndHonorsTheRequestedDirection() {
        Vector shortForward = GuardianFlightTrajectory.forwardCancel(
                new Vector(1D, .5D, 0D), 0D);
        Vector longForward = GuardianFlightTrajectory.forwardCancel(
                new Vector(1D, .5D, 0D), 18D);
        Vector upward = GuardianFlightTrajectory.upwardCancel(
                new Vector(1D, -.4D, 1D), 18D);

        assertTrue(longForward.length() > shortForward.length());
        assertEquals(shortForward.getX() / shortForward.getY(),
                longForward.getX() / longForward.getY(), 1.0E-9D);
        assertTrue(upward.getY() > 1D);
        assertEquals(.35D, upward.getX(), 1.0E-9D);
        assertEquals(.35D, upward.getZ(), 1.0E-9D);
    }
}
