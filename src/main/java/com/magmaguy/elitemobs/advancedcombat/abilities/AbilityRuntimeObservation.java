package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Stable, data-only evidence emitted at authoritative gameplay boundaries. */
public record AbilityRuntimeObservation(
        Kind kind,
        UUID casterId,
        UUID targetId,
        String abilityId,
        double amount,
        int durationTicks,
        int count,
        Set<AbilityEffect> effects,
        Set<AbilityMechanic> mechanics) {
    public AbilityRuntimeObservation {
        kind = Objects.requireNonNull(kind, "kind");
        casterId = Objects.requireNonNull(casterId, "casterId");
        abilityId = Objects.requireNonNull(abilityId, "abilityId");
        if (!Double.isFinite(amount)) throw new IllegalArgumentException("amount must be finite");
        if (durationTicks < 0 || count < 0)
            throw new IllegalArgumentException("duration and count must be non-negative");
        effects = Set.copyOf(effects);
        mechanics = Set.copyOf(mechanics);
    }

    public static AbilityRuntimeObservation effect(
            Kind kind,
            UUID casterId,
            UUID targetId,
            String abilityId,
            double amount,
            int durationTicks,
            AbilityEffect effect) {
        return new AbilityRuntimeObservation(kind, casterId, targetId, abilityId,
                amount, durationTicks, 1, Set.of(effect), Set.of());
    }

    public static AbilityRuntimeObservation state(
            Kind kind,
            UUID casterId,
            UUID targetId,
            String abilityId,
            double amount,
            int durationTicks,
            AbilityMechanic mechanic) {
        if (kind != Kind.STATE_ARMED && kind != Kind.STATE_CONSUMED
                && kind != Kind.RETALIATION_STORED && kind != Kind.RETALIATION_RELEASED)
            throw new IllegalArgumentException("Not a state observation kind: " + kind);
        return new AbilityRuntimeObservation(kind, casterId, targetId, abilityId,
                amount, durationTicks, 1, Set.of(), Set.of(mechanic));
    }

    public enum Kind {
        DAMAGE,
        HEAL,
        SHIELD,
        CONTROL,
        TAUNT,
        STATUS_APPLIED,
        MODIFIER_APPLIED,
        MODIFIER_TRIGGERED,
        STATE_ARMED,
        STATE_CONSUMED,
        DAMAGE_REDIRECTED,
        DAMAGE_SHARED,
        STATUS_BLOCKED,
        RETALIATION_STORED,
        RETALIATION_RELEASED,
        SUMMON_SPAWNED,
        SUMMON_CLEARED,
        CONSTRUCT_SPAWNED,
        CONSTRUCT_CLEARED,
        FIELD_PULSE,
        // One delivery; amount counts its selected enemies, including zero for a miss.
        PROJECTILE_IMPACT,
        CAST_FAILED,
        RESOURCE_SPENT,
        RESOURCE_GAINED,
        RESOURCE_DELTA,
        LIFECYCLE_CLEARED
    }
}
