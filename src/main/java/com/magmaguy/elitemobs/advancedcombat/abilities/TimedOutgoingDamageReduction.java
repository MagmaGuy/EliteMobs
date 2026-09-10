package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.function.Predicate;

/**
 * Owns time-bounded percentage reductions to an entity's outgoing damage.
 *
 * <p>Callers author a multiplier at neutral level. This module applies the shared class-level
 * curve, keeps one lease per source ability, selects the strongest live lease without stacking,
 * and reports the exact damage prevented by each hit.</p>
 */
final class TimedOutgoingDamageReduction implements AutoCloseable {
    private static final long NANOS_PER_TICK = 50_000_000L;

    private final LongSupplier nanoTime;
    private final Map<UUID, Map<LeaseKey, Lease>> leasesByTarget = new HashMap<>();
    private boolean closed;

    TimedOutgoingDamageReduction(LongSupplier nanoTime) {
        this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
    }

    double register(
            UUID sourceId,
            UUID targetId,
            String abilityId,
            double authoredMultiplier,
            int effectiveLevel,
            int durationTicks) {
        return register(sourceId, targetId, abilityId, authoredMultiplier,
                effectiveLevel, 1D, durationTicks);
    }

    double register(
            UUID sourceId,
            UUID targetId,
            String abilityId,
            double authoredMultiplier,
            int effectiveLevel,
            double potencyMultiplier,
            int durationTicks) {
        Objects.requireNonNull(sourceId, "sourceId");
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(abilityId, "abilityId");
        if (!Double.isFinite(authoredMultiplier)
                || authoredMultiplier < 0D
                || authoredMultiplier >= 1D)
            throw new IllegalArgumentException("authoredMultiplier must be within [0, 1)");
        if (effectiveLevel < 1) throw new IllegalArgumentException("effectiveLevel must be positive");
        if (!Double.isFinite(potencyMultiplier) || potencyMultiplier <= 0D)
            throw new IllegalArgumentException("potencyMultiplier must be finite and positive");
        if (durationTicks <= 0) throw new IllegalArgumentException("durationTicks must be positive");
        if (closed) throw new IllegalStateException("Damage-reduction ledger is closed");

        double potencyAdjusted = 1D - Math.min(.95D,
                (1D - authoredMultiplier) * potencyMultiplier);
        double multiplier = ActiveAbilityLevelScaling.modifier(potencyAdjusted, effectiveLevel);
        LeaseKey key = new LeaseKey(sourceId, abilityId);
        leasesByTarget.computeIfAbsent(targetId, ignored -> new HashMap<>())
                .put(key, new Lease(sourceId, abilityId, multiplier, expiresAt(durationTicks)));
        return multiplier;
    }

    Optional<Application> apply(
            UUID targetId,
            double originalDamage,
            boolean recipientEligible,
            Predicate<UUID> liveSource) {
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(liveSource, "liveSource");
        if (closed) return Optional.empty();
        // A class user may weaken an Elite in an ordinary world while another player has not
        // opted into outside-world class mechanics. The debuff belongs to [Alpha] Advanced Combat System;
        // it must not silently alter damage dealt to that non-participant.
        if (!recipientEligible) return Optional.empty();
        if (!Double.isFinite(originalDamage) || originalDamage <= 0D) return Optional.empty();
        Map<LeaseKey, Lease> leases = leasesByTarget.get(targetId);
        if (leases == null) return Optional.empty();
        long now = nanoTime.getAsLong();
        leases.entrySet().removeIf(entry -> entry.getValue().expiresAtNanos() <= now
                || !liveSource.test(entry.getValue().sourceId()));
        if (leases.isEmpty()) {
            leasesByTarget.remove(targetId);
            return Optional.empty();
        }
        Lease strongest = leases.values().stream()
                .min(java.util.Comparator.comparingDouble(Lease::multiplier))
                .orElseThrow();
        double modifiedDamage = Math.max(0D, originalDamage * strongest.multiplier());
        return Optional.of(new Application(
                strongest.sourceId(), targetId, strongest.abilityId(), strongest.multiplier(),
                originalDamage, modifiedDamage));
    }

    void clearSource(UUID sourceId) {
        Objects.requireNonNull(sourceId, "sourceId");
        if (closed) return;
        leasesByTarget.entrySet().removeIf(entry -> {
            entry.getValue().entrySet().removeIf(lease -> lease.getKey().sourceId().equals(sourceId));
            return entry.getValue().isEmpty();
        });
    }

    void clearTarget(UUID targetId) {
        Objects.requireNonNull(targetId, "targetId");
        if (!closed) leasesByTarget.remove(targetId);
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        leasesByTarget.clear();
    }

    private long expiresAt(int durationTicks) {
        long now = nanoTime.getAsLong();
        long durationNanos = durationTicks * NANOS_PER_TICK;
        try {
            return Math.addExact(now, durationNanos);
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }

    record Application(
            UUID sourceId,
            UUID targetId,
            String abilityId,
            double multiplier,
            double originalDamage,
            double modifiedDamage) {
        double reducedDamage() {
            return Math.max(0D, originalDamage - modifiedDamage);
        }
    }

    private record LeaseKey(UUID sourceId, String abilityId) {
    }

    private record Lease(
            UUID sourceId,
            String abilityId,
            double multiplier,
            long expiresAtNanos) {
    }
}
