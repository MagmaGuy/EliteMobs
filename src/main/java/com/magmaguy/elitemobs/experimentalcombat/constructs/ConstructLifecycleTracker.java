package com.magmaguy.elitemobs.experimentalcombat.constructs;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Exact-once ownership ledger for packet-construct spawn and removal evidence. */
final class ConstructLifecycleTracker {
    private final Map<UUID, Lease> active = new HashMap<>();

    Lease open(
            UUID constructId,
            UUID casterId,
            String abilityId,
            int durationTicks,
            ClassConstructVisualRegistry.AnchorMode anchorMode) {
        Lease lease = new Lease(constructId, casterId, abilityId, durationTicks, anchorMode);
        if (active.putIfAbsent(constructId, lease) != null)
            throw new IllegalStateException("Duplicate construct lease " + constructId);
        return lease;
    }

    Optional<Lease> close(UUID constructId) {
        return Optional.ofNullable(active.remove(constructId));
    }

    int size() {
        return active.size();
    }

    record Lease(
            UUID constructId,
            UUID casterId,
            String abilityId,
            int durationTicks,
            ClassConstructVisualRegistry.AnchorMode anchorMode) {
        Lease {
            Objects.requireNonNull(constructId, "constructId");
            Objects.requireNonNull(casterId, "casterId");
            Objects.requireNonNull(abilityId, "abilityId");
            Objects.requireNonNull(anchorMode, "anchorMode");
            if (durationTicks < 1) throw new IllegalArgumentException("durationTicks must be positive");
        }
    }
}
