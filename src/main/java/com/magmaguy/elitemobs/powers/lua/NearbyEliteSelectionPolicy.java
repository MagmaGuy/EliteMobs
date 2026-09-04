package com.magmaguy.elitemobs.powers.lua;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Bounded, deterministic selection policy for Lua queries over tracked EliteMobs actors. */
final class NearbyEliteSelectionPolicy {

    static final double MAXIMUM_RADIUS = 64.0D;
    static final int MAXIMUM_RESULTS = 32;

    private NearbyEliteSelectionPolicy() {
    }

    static List<Candidate> select(
            UUID sourceWorldId,
            double radius,
            List<Candidate> candidates) {
        Objects.requireNonNull(sourceWorldId, "sourceWorldId");
        Objects.requireNonNull(candidates, "candidates");
        if (!Double.isFinite(radius) || radius <= 0 || radius > MAXIMUM_RADIUS) {
            throw new IllegalArgumentException(
                    "Elite query radius must be finite and between 0 and " + MAXIMUM_RADIUS);
        }
        double radiusSquared = radius * radius;
        return candidates.stream()
                .filter(Candidate::tracked)
                .filter(Candidate::alive)
                .filter(candidate -> candidate.worldId().equals(sourceWorldId))
                .filter(candidate -> candidate.distanceSquared() <= radiusSquared)
                .sorted(Comparator.comparingDouble(Candidate::distanceSquared)
                        .thenComparing(Candidate::entityId))
                .limit(MAXIMUM_RESULTS)
                .toList();
    }

    record Candidate(
            UUID entityId,
            UUID worldId,
            boolean tracked,
            boolean alive,
            double distanceSquared) {

        Candidate {
            Objects.requireNonNull(entityId, "entityId");
            Objects.requireNonNull(worldId, "worldId");
            if (!Double.isFinite(distanceSquared) || distanceSquared < 0) {
                throw new IllegalArgumentException(
                        "Candidate distance must be finite and non-negative");
            }
        }
    }
}
