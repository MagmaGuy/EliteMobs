package com.magmaguy.elitemobs.advancedcombat.abilities;

import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

/** Pure forward ground scan used by Blink before Bukkit collision validation. */
final class GroundBlinkPath {
    static final double SCAN_STEP = .25D;
    static final double MINIMUM_DISTANCE = .5D;
    private static final double MAXIMUM_HEIGHT_CHANGE = 1D;
    private static final double EPSILON = 1.0E-7D;

    private GroundBlinkPath() {
    }

    static Optional<Destination> scan(
            Vector start,
            Vector lookDirection,
            double maximumDistance,
            StandingHeightResolver heightResolver) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(lookDirection, "lookDirection");
        Objects.requireNonNull(heightResolver, "heightResolver");
        if (!finite(start) || !finite(lookDirection)
                || !Double.isFinite(maximumDistance)
                || maximumDistance < MINIMUM_DISTANCE) {
            return Optional.empty();
        }

        Vector direction = lookDirection.clone().setY(0D);
        if (direction.lengthSquared() < EPSILON) return Optional.empty();
        direction.normalize();

        Destination lastValid = null;
        double previousY = start.getY();
        int steps = Math.max(1, (int) Math.ceil(maximumDistance / SCAN_STEP));
        for (int step = 1; step <= steps; step++) {
            double distance = Math.min(step * SCAN_STEP, maximumDistance);
            double x = start.getX() + direction.getX() * distance;
            double z = start.getZ() + direction.getZ() * distance;
            OptionalDouble standingY = heightResolver.resolve(x, z, previousY);
            if (standingY.isEmpty()
                    || !Double.isFinite(standingY.getAsDouble())
                    || Math.abs(standingY.getAsDouble() - previousY)
                    > MAXIMUM_HEIGHT_CHANGE + EPSILON) {
                break;
            }

            previousY = standingY.getAsDouble();
            lastValid = new Destination(x, previousY, z, distance);
        }
        return lastValid == null || lastValid.distance() + EPSILON < MINIMUM_DISTANCE
                ? Optional.empty()
                : Optional.of(lastValid);
    }

    private static boolean finite(Vector vector) {
        return Double.isFinite(vector.getX())
                && Double.isFinite(vector.getY())
                && Double.isFinite(vector.getZ());
    }

    @FunctionalInterface
    interface StandingHeightResolver {
        OptionalDouble resolve(double x, double z, double previousY);
    }

    record Destination(double x, double y, double z, double distance) {
        Destination {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                    || !Double.isFinite(distance) || distance < 0D) {
                throw new IllegalArgumentException("Blink destination values must be finite");
            }
        }
    }
}
