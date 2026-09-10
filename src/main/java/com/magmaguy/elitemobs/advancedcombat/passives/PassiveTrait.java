package com.magmaguy.elitemobs.advancedcombat.passives;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/** A scalable passive contribution guarded by an AND-set of runtime conditions. */
public record PassiveTrait(Set<PassiveCondition> conditions, PassiveProfile profile) {

    public PassiveTrait {
        Objects.requireNonNull(conditions, "conditions");
        Objects.requireNonNull(profile, "profile");
        conditions = conditions.isEmpty()
                ? Set.of(PassiveCondition.ALWAYS)
                : Set.copyOf(EnumSet.copyOf(conditions));
    }

    public boolean unconditional() {
        return conditions.size() == 1 && conditions.contains(PassiveCondition.ALWAYS);
    }

    public boolean matches(PassiveConditionContext context) {
        Objects.requireNonNull(context, "context");
        return conditions.stream().allMatch(condition -> condition.matches(context));
    }
}
