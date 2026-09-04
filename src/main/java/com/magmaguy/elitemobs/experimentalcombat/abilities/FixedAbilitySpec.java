package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;

import java.util.Objects;
import java.util.Set;

/** Active abilities have no cooldowns: pacing comes entirely from the resource cost. */
public record FixedAbilitySpec(
        String id,
        AbilitySlot slot,
        AbilityFamily family,
        AbilityTarget target,
        Set<AbilityEffect> effects,
        AbilityTuning tuning,
        AbilityExecutionTraits executionTraits,
        double resourceCost) {

    public FixedAbilitySpec {
        Objects.requireNonNull(id, "id");
        slot = Objects.requireNonNull(slot, "slot");
        family = Objects.requireNonNull(family, "family");
        target = Objects.requireNonNull(target, "target");
        effects = Set.copyOf(effects);
        if (effects.isEmpty() && family == AbilityFamily.INSTANT)
            throw new IllegalArgumentException("Instant ability needs an effect: " + id);
        tuning = Objects.requireNonNull(tuning, "tuning");
        executionTraits = Objects.requireNonNull(executionTraits, "executionTraits");
        if (!Double.isFinite(resourceCost) || resourceCost <= 0D || resourceCost > 100D)
            throw new IllegalArgumentException("resourceCost must be within (0, 100]: " + id);
    }

    FixedAbilitySpec withMechanics(AbilityMechanic... mechanics) {
        return new FixedAbilitySpec(id, slot, family, target, effects, tuning,
                executionTraits.plus(mechanics), resourceCost);
    }

    FixedAbilitySpec withResourceCost(double amount) {
        return new FixedAbilitySpec(id, slot, family, target, effects, tuning, executionTraits,
                amount);
    }

    FixedAbilitySpec withWeakenMultiplier(double multiplier) {
        return new FixedAbilitySpec(id, slot, family, target, effects,
                tuning.withWeakenMultiplier(multiplier), executionTraits,
                resourceCost);
    }
}
