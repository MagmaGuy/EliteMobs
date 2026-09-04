package com.magmaguy.elitemobs.experimentalcombat.minions;

import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityEffect;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityFamily;
import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilitySpec;

import java.util.EnumSet;
import java.util.Set;

/** Pure, probeable translation from summon specs to on-hit servant behavior. */
public record ClassMinionImpactPlan(
        double lifestealFraction,
        Set<AbilityEffect> controlEffects) {
    private static final double STANDARD_LIFESTEAL_FRACTION = .14D;
    private static final Set<AbilityEffect> SUPPORTED_CONTROL_EFFECTS = EnumSet.of(
            AbilityEffect.SLOW, AbilityEffect.WEAKEN,
            AbilityEffect.INTERRUPT, AbilityEffect.FEAR);

    public ClassMinionImpactPlan {
        if (!Double.isFinite(lifestealFraction) || lifestealFraction < 0D || lifestealFraction > 1D)
            throw new IllegalArgumentException("lifestealFraction must be within [0, 1]");
        controlEffects = Set.copyOf(controlEffects);
    }

    public static ClassMinionImpactPlan from(FixedAbilitySpec spec) {
        if (spec.family() != AbilityFamily.SUMMON)
            throw new IllegalArgumentException("Only summon abilities have servant impacts: " + spec.id());
        EnumSet<AbilityEffect> controls = EnumSet.noneOf(AbilityEffect.class);
        controls.addAll(spec.effects());
        controls.retainAll(SUPPORTED_CONTROL_EFFECTS);
        return new ClassMinionImpactPlan(
                spec.effects().contains(AbilityEffect.LIFESTEAL)
                        ? STANDARD_LIFESTEAL_FRACTION : 0D,
                controls);
    }
}
