package com.magmaguy.elitemobs.experimentalcombat.abilities;

import java.util.Set;

/** Transactional post-spend effects committed only after the engine accepts an activation. */
public record AbilityCommitEffects(double resourceGrant) {
    private static final double RESOURCE_BURST_BASE = 40D;
    private static final double RESOURCE_BURST_PER_LEVEL = .05D;

    public static final AbilityCommitEffects NONE = new AbilityCommitEffects(0D);

    public AbilityCommitEffects {
        if (!Double.isFinite(resourceGrant) || resourceGrant < 0D)
            throw new IllegalArgumentException("resourceGrant must be finite and non-negative");
    }

    public static AbilityCommitEffects resolve(FixedAbilitySpec spec, int effectiveLevel) {
        Set<AbilityMechanic> mechanics = spec.executionTraits().mechanics();
        double resource = mechanics.contains(AbilityMechanic.RESOURCE_BURST)
                ? RESOURCE_BURST_BASE + Math.max(0, effectiveLevel) * RESOURCE_BURST_PER_LEVEL
                : 0D;
        return resource == 0D ? NONE : new AbilityCommitEffects(resource);
    }
}
