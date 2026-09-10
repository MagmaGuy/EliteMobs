package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Complete lifecycle manifest for source-scoped ability state. Adding a state bucket without both
 * exit and shutdown cleanup fails at construction instead of leaking into another run.
 */
final class AbilityStateOwnership {
    enum Bucket {
        REDIRECTS,
        DEATH_GUARDS,
        TRIGGERED_BARRIERS,
        TRIGGERED_PROTECTIONS,
        DAMAGE_SHARES,
        LIFESTEAL_WINDOWS,
        CONTROL_IMMUNITY,
        DEBUFF_IMMUNITY,
        PLANTED_GUARDS,
        DETONATING_MARKS,
        RETALIATION,
        RECENT_ATTACKERS,
        TAUNTS,
        DEFENSE_BREAKS,
        ACTIVE_FIELDS,
        ACTIVE_WIND_UPS,
        PENDING_HEAL_ECHOES,
        LIFECYCLE_GENERATIONS,
        REDISTRIBUTION_GUARDS,
        OWNED_VELOCITIES
    }

    private final Map<Bucket, Binding> bindings;

    private AbilityStateOwnership(Map<Bucket, Binding> bindings) {
        this.bindings = Map.copyOf(bindings);
    }

    static Builder builder() {
        return new Builder();
    }

    void clearSource(UUID sourceId) {
        Objects.requireNonNull(sourceId, "sourceId");
        bindings.values().forEach(binding -> binding.clearSource().accept(sourceId));
    }

    void clearAll() {
        bindings.values().forEach(binding -> binding.clearAll().run());
    }

    static final class Builder {
        private final Map<Bucket, Binding> bindings = new EnumMap<>(Bucket.class);

        Builder bind(Bucket bucket, Consumer<UUID> clearSource, Runnable clearAll) {
            Binding previous = bindings.putIfAbsent(
                    Objects.requireNonNull(bucket, "bucket"),
                    new Binding(
                            Objects.requireNonNull(clearSource, "clearSource"),
                            Objects.requireNonNull(clearAll, "clearAll")));
            if (previous != null) throw new IllegalStateException("Duplicate state binding for " + bucket);
            return this;
        }

        AbilityStateOwnership build() {
            EnumSet<Bucket> missing = EnumSet.allOf(Bucket.class);
            missing.removeAll(bindings.keySet());
            if (!missing.isEmpty()) throw new IllegalStateException("Missing state cleanup bindings: " + missing);
            return new AbilityStateOwnership(bindings);
        }
    }

    private record Binding(Consumer<UUID> clearSource, Runnable clearAll) {
    }
}
