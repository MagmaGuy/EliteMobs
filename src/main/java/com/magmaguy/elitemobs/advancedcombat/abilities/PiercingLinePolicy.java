package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Pure ray-to-hitbox intersection ordering for line-piercing class projectiles. */
final class PiercingLinePolicy {
    private static final double EPSILON = 1.0E-9D;

    private PiercingLinePolicy() {
    }

    static <T> List<Intersection<T>> intersections(
            Point origin,
            Point direction,
            double maximumDistance,
            List<Candidate<T>> candidates) {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(direction, "direction");
        Objects.requireNonNull(candidates, "candidates");
        if (!Double.isFinite(maximumDistance) || maximumDistance <= 0D) return List.of();
        double length = Math.sqrt(direction.x() * direction.x()
                + direction.y() * direction.y() + direction.z() * direction.z());
        if (!Double.isFinite(length) || length < EPSILON) return List.of();
        Point normalized = new Point(
                direction.x() / length, direction.y() / length, direction.z() / length);
        List<Intersection<T>> hits = new ArrayList<>();
        for (Candidate<T> candidate : candidates) {
            double distance = entryDistance(origin, normalized, candidate.bounds(), maximumDistance);
            if (Double.isFinite(distance)) hits.add(new Intersection<>(candidate.target(), distance));
        }
        hits.sort(Comparator.comparingDouble(Intersection::distance));
        return List.copyOf(hits);
    }

    private static double entryDistance(Point origin, Point direction, Box box, double maximumDistance) {
        double minimum = 0D;
        double maximum = maximumDistance;
        double[] origins = {origin.x(), origin.y(), origin.z()};
        double[] directions = {direction.x(), direction.y(), direction.z()};
        double[] lower = {box.minimumX(), box.minimumY(), box.minimumZ()};
        double[] upper = {box.maximumX(), box.maximumY(), box.maximumZ()};
        for (int axis = 0; axis < 3; axis++) {
            if (Math.abs(directions[axis]) < EPSILON) {
                if (origins[axis] < lower[axis] || origins[axis] > upper[axis]) return Double.NaN;
                continue;
            }
            double first = (lower[axis] - origins[axis]) / directions[axis];
            double second = (upper[axis] - origins[axis]) / directions[axis];
            if (first > second) {
                double swap = first;
                first = second;
                second = swap;
            }
            minimum = Math.max(minimum, first);
            maximum = Math.min(maximum, second);
            if (maximum + EPSILON < minimum) return Double.NaN;
        }
        return minimum <= maximumDistance + EPSILON ? Math.max(0D, minimum) : Double.NaN;
    }

    record Point(double x, double y, double z) {
    }

    record Box(
            double minimumX,
            double minimumY,
            double minimumZ,
            double maximumX,
            double maximumY,
            double maximumZ) {
        Box {
            if (minimumX > maximumX || minimumY > maximumY || minimumZ > maximumZ)
                throw new IllegalArgumentException("box minima must not exceed maxima");
        }
    }

    record Candidate<T>(T target, Box bounds) {
        Candidate {
            Objects.requireNonNull(target, "target");
            Objects.requireNonNull(bounds, "bounds");
        }
    }

    record Intersection<T>(T target, double distance) {
    }
}
