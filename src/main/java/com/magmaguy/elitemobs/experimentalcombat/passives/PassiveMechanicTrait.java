package com.magmaguy.elitemobs.experimentalcombat.passives;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/** A typed mechanic contribution guarded by the same runtime facts as numerical traits. */
public record PassiveMechanicTrait(
        Set<PassiveCondition> conditions,
        PassiveMechanics authoredAtLevelThirty) {

    public PassiveMechanicTrait {
        Objects.requireNonNull(conditions, "conditions");
        Objects.requireNonNull(authoredAtLevelThirty, "authoredAtLevelThirty");
        conditions = conditions.isEmpty()
                ? Set.of(PassiveCondition.ALWAYS)
                : Set.copyOf(EnumSet.copyOf(conditions));
    }

    public boolean unconditional() {
        return conditions.size() == 1 && conditions.contains(PassiveCondition.ALWAYS);
    }

    public PassiveMechanics atContributionLevel(int level) {
        if (level < 1) throw new IllegalArgumentException("Passive contribution level must be positive");
        return authoredAtLevelThirty.scaled(.70D + level * .01D);
    }
}
