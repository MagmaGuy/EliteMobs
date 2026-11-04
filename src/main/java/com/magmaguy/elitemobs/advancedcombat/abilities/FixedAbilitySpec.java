package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
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
        double resourceCost,
        OrdinaryFoeVariant ordinaryFoeVariant) {

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
        boolean declaresVariant = executionTraits.mechanics().contains(AbilityMechanic.ORDINARY_FOE_VARIANT);
        if (declaresVariant != (ordinaryFoeVariant != null))
            throw new IllegalArgumentException(
                    "ORDINARY_FOE_VARIANT and an ordinary-foe payload must be declared together: " + id);
    }

    public FixedAbilitySpec(
            String id,
            AbilitySlot slot,
            AbilityFamily family,
            AbilityTarget target,
            Set<AbilityEffect> effects,
            AbilityTuning tuning,
            AbilityExecutionTraits executionTraits,
            double resourceCost) {
        this(id, slot, family, target, effects, tuning, executionTraits, resourceCost, null);
    }

    public Optional<OrdinaryFoeVariant> ordinaryFoes() {
        return Optional.ofNullable(ordinaryFoeVariant);
    }

    /** Every effect this ability can apply, including the ordinary-foe payload. */
    public Set<AbilityEffect> declaredEffects() {
        if (ordinaryFoeVariant == null) return effects;
        EnumSet<AbilityEffect> declared = EnumSet.noneOf(AbilityEffect.class);
        declared.addAll(effects);
        declared.addAll(ordinaryFoeVariant.effects());
        return declared;
    }

    FixedAbilitySpec withMechanics(AbilityMechanic... mechanics) {
        return new FixedAbilitySpec(id, slot, family, target, effects, tuning,
                executionTraits.plus(mechanics), resourceCost, ordinaryFoeVariant);
    }

    FixedAbilitySpec withResourceCost(double amount) {
        return new FixedAbilitySpec(id, slot, family, target, effects, tuning, executionTraits,
                amount, ordinaryFoeVariant);
    }

    FixedAbilitySpec withWeakenMultiplier(double multiplier) {
        return new FixedAbilitySpec(id, slot, family, target, effects,
                tuning.withWeakenMultiplier(multiplier), executionTraits,
                resourceCost, ordinaryFoeVariant);
    }

    FixedAbilitySpec withOrdinaryFoeVariant(OrdinaryFoeVariant variant) {
        Objects.requireNonNull(variant, "variant");
        return new FixedAbilitySpec(id, slot, family, target, effects, tuning,
                executionTraits.plus(AbilityMechanic.ORDINARY_FOE_VARIANT), resourceCost, variant);
    }
}
