package com.magmaguy.elitemobs.advancedcombat.minions;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Pure sight-line selector with a forgiving forward-cone fallback. */
public final class CorpseTargetSelector {
    private static final double PRECISE_CORRIDOR = 1.75D;
    private static final double FALLBACK_MINIMUM_COSINE = .65D;

    private CorpseTargetSelector() {
    }

    public static Optional<UUID> select(
            Point origin,
            Point lookDirection,
            double range,
            List<Candidate> candidates) {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(lookDirection, "lookDirection");
        Objects.requireNonNull(candidates, "candidates");
        if (!Double.isFinite(range) || range <= 0D) return Optional.empty();

        Point direction = lookDirection.normalized();
        List<Scored> forward = candidates.stream()
                .map(candidate -> score(origin, direction, range, candidate))
                .filter(Objects::nonNull)
                .toList();
        return forward.stream()
                .filter(candidate -> candidate.perpendicular() <= PRECISE_CORRIDOR)
                .min(Comparator.comparingDouble(Scored::perpendicular)
                        .thenComparingDouble(Scored::distance))
                .or(() -> forward.stream()
                        .filter(candidate -> candidate.cosine() >= FALLBACK_MINIMUM_COSINE)
                        .min(Comparator.comparingDouble(Scored::cosine).reversed()
                                .thenComparingDouble(Scored::distance)))
                .map(candidate -> candidate.candidate().id());
    }

    private static Scored score(Point origin, Point direction, double range, Candidate candidate) {
        Point delta = candidate.position().subtract(origin);
        double distance = delta.length();
        if (distance <= 0D || distance > range) return null;
        double projection = delta.dot(direction);
        if (projection <= 0D) return null;
        double perpendicularSquared = Math.max(0D, delta.dot(delta) - projection * projection);
        return new Scored(candidate, distance, Math.sqrt(perpendicularSquared), projection / distance);
    }

    public record Candidate(UUID id, Point position) {
        public Candidate {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(position, "position");
        }
    }

    public record Point(double x, double y, double z) {
        public Point {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new IllegalArgumentException("Point coordinates must be finite");
            }
        }

        Point subtract(Point other) {
            return new Point(x - other.x, y - other.y, z - other.z);
        }

        double dot(Point other) {
            return x * other.x + y * other.y + z * other.z;
        }

        double length() {
            return Math.sqrt(dot(this));
        }

        Point normalized() {
            double length = length();
            if (length <= 1.0E-9D) throw new IllegalArgumentException("Look direction cannot be zero");
            return new Point(x / length, y / length, z / length);
        }
    }

    private record Scored(Candidate candidate, double distance, double perpendicular, double cosine) {
    }
}
