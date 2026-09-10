package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Pure lease update policy shared by taunt and defense-break ownership maps. */
final class TimedStatusLeasePolicy {
    private TimedStatusLeasePolicy() {
    }

    static <K> Set<K> apply(
            Map<K, Long> expiryByTarget,
            Collection<? extends K> targets,
            long now,
            long duration,
            boolean extendExisting) {
        Objects.requireNonNull(expiryByTarget, "expiryByTarget");
        Objects.requireNonNull(targets, "targets");
        if (duration < 1L) throw new IllegalArgumentException("duration must be positive");
        expiryByTarget.entrySet().removeIf(entry -> entry.getValue() <= now);
        Set<K> extended = new LinkedHashSet<>();
        for (K target : targets) {
            Objects.requireNonNull(target, "targets contains null");
            Long existing = expiryByTarget.get(target);
            if (extendExisting && existing != null && existing > now) extended.add(target);
            long base = extendExisting && existing != null ? Math.max(now, existing) : now;
            expiryByTarget.put(target, saturatedAdd(base, duration));
        }
        return Set.copyOf(extended);
    }

    private static long saturatedAdd(long value, long increment) {
        return value > Long.MAX_VALUE - increment ? Long.MAX_VALUE : value + increment;
    }
}
