package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Builds exact per-effect evidence after control application has succeeded. */
final class AbilityControlEvidence {
    private static final Set<AbilityEffect> CONTROLS = EnumSet.of(
            AbilityEffect.KNOCKBACK, AbilityEffect.PULL, AbilityEffect.LAUNCH,
            AbilityEffect.SLOW, AbilityEffect.WEAKEN, AbilityEffect.GLOW,
            AbilityEffect.INTERRUPT, AbilityEffect.FEAR, AbilityEffect.ROOT,
            AbilityEffect.BURN);

    private AbilityControlEvidence() {
    }

    static List<AbilityRuntimeObservation> applied(
            UUID casterId,
            UUID targetId,
            FixedAbilitySpec spec,
            Collection<AbilityEffect> successfulEffects,
            double potency,
            int durationTicks) {
        Objects.requireNonNull(casterId, "casterId");
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(spec, "spec");
        Objects.requireNonNull(successfulEffects, "successfulEffects");
        List<AbilityRuntimeObservation> observations = new ArrayList<>();
        for (AbilityEffect effect : successfulEffects) {
            if (!CONTROLS.contains(effect))
                throw new IllegalArgumentException("Not a control effect: " + effect);
            if (!spec.effects().contains(effect))
                throw new IllegalArgumentException(spec.id() + " does not declare " + effect);
            observations.add(new AbilityRuntimeObservation(
                    AbilityRuntimeObservation.Kind.CONTROL,
                    casterId, targetId, spec.id(), potency,
                    Math.max(0, durationTicks), 1,
                    Set.of(effect), spec.executionTraits().mechanics()));
        }
        return List.copyOf(observations);
    }

    static AbilityRuntimeObservation mechanicTriggered(
            UUID casterId,
            UUID targetId,
            FixedAbilitySpec spec,
            AbilityMechanic mechanic,
            AbilityEffect effect,
            double amount,
            int durationTicks) {
        if (!spec.executionTraits().mechanics().contains(mechanic))
            throw new IllegalArgumentException(spec.id() + " does not declare " + mechanic);
        if (!spec.effects().contains(effect))
            throw new IllegalArgumentException(spec.id() + " does not declare " + effect);
        return new AbilityRuntimeObservation(
                AbilityRuntimeObservation.Kind.MODIFIER_TRIGGERED,
                casterId, targetId, spec.id(), amount,
                Math.max(0, durationTicks), 1,
                Set.of(effect), Set.of(mechanic));
    }
}
