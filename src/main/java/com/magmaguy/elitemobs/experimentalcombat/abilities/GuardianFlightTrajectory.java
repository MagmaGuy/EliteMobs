package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.bukkit.util.Vector;

/** Pure steering and cancel-momentum math for Guardian Flight. */
final class GuardianFlightTrajectory {
    private static final double STEERING_ACCELERATION = .22D;
    private static final double MINIMUM_TRAVEL_SPEED = .32D;
    private static final double MAXIMUM_TRAVEL_SPEED = 1.3D;
    private static final double FORWARD_CANCEL_MINIMUM_SPEED = .65D;
    private static final double FORWARD_CANCEL_MAXIMUM_SPEED = 1.45D;
    private static final double FORWARD_CANCEL_SPEED_PER_BLOCK = .045D;
    private static final double UPWARD_CANCEL_BASE_SPEED = .78D;
    private static final double UPWARD_CANCEL_MAXIMUM_BONUS = .32D;

    private GuardianFlightTrajectory() {
    }

    static Vector steer(Vector currentVelocity, Vector displacement) {
        Vector current = finite(currentVelocity) ? currentVelocity.clone() : new Vector();
        Vector remaining = finite(displacement) ? displacement.clone() : new Vector();
        double distance = remaining.length();
        if (distance <= 1.0E-9D) return current;

        double desiredSpeed = clamp(
                distance * .28D,
                MINIMUM_TRAVEL_SPEED,
                MAXIMUM_TRAVEL_SPEED);
        Vector desired = remaining.multiply(1D / distance).multiply(desiredSpeed);
        Vector steering = desired.subtract(current);
        if (steering.lengthSquared() > STEERING_ACCELERATION * STEERING_ACCELERATION)
            steering.normalize().multiply(STEERING_ACCELERATION);

        Vector next = current.clone().add(steering);
        double currentSpeed = current.length();
        double nextSpeed = next.length();
        double allowedSpeed = Math.max(MAXIMUM_TRAVEL_SPEED, currentSpeed);
        if (nextSpeed > allowedSpeed && nextSpeed > 1.0E-9D)
            next.multiply(allowedSpeed / nextSpeed);
        return next;
    }

    static Vector forwardCancel(Vector viewDirection, double travelledDistance) {
        Vector direction = finite(viewDirection) ? viewDirection.clone() : new Vector();
        if (direction.lengthSquared() <= 1.0E-9D) direction.setZ(1D);
        double speed = clamp(
                FORWARD_CANCEL_MINIMUM_SPEED
                        + Math.max(0D, travelledDistance) * FORWARD_CANCEL_SPEED_PER_BLOCK,
                FORWARD_CANCEL_MINIMUM_SPEED,
                FORWARD_CANCEL_MAXIMUM_SPEED);
        return direction.normalize().multiply(speed);
    }

    static Vector upwardCancel(Vector currentVelocity, double travelledDistance) {
        Vector momentum = finite(currentVelocity) ? currentVelocity.clone() : new Vector();
        momentum.setX(momentum.getX() * .35D);
        momentum.setZ(momentum.getZ() * .35D);
        momentum.setY(UPWARD_CANCEL_BASE_SPEED + Math.min(
                UPWARD_CANCEL_MAXIMUM_BONUS,
                Math.max(0D, travelledDistance) * .02D));
        return momentum;
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static boolean finite(Vector vector) {
        return vector != null
                && Double.isFinite(vector.getX())
                && Double.isFinite(vector.getY())
                && Double.isFinite(vector.getZ());
    }
}
