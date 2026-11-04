package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Objects;
import java.util.Set;

/**
 * Payload an ability applies to ordinary foes while large foes and bosses keep the main payload.
 *
 * <p>Abilities built around big targets use this to stay useful against everything else without
 * losing their identity. Targeting is shared; only the per-target effects and tuning differ.</p>
 */
public record OrdinaryFoeVariant(Set<AbilityEffect> effects, AbilityTuning tuning) {
    public OrdinaryFoeVariant {
        effects = Set.copyOf(Objects.requireNonNull(effects, "effects"));
        tuning = Objects.requireNonNull(tuning, "tuning");
        if (effects.isEmpty()) throw new IllegalArgumentException("An ordinary-foe variant needs an effect");
    }
}
