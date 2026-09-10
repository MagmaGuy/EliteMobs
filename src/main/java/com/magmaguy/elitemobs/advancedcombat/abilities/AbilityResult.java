package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Objects;

/** Typed result used by the caller to decide whether the resource spend should be committed. */
public record AbilityResult(
        boolean successful,
        String abilityId,
        AbilityFailureReason failureReason,
        AbilityContribution contribution,
        boolean continuesAsynchronously) {

    public AbilityResult {
        Objects.requireNonNull(abilityId, "abilityId");
        failureReason = Objects.requireNonNull(failureReason, "failureReason");
        contribution = Objects.requireNonNull(contribution, "contribution");
        if (successful != (failureReason == AbilityFailureReason.NONE))
            throw new IllegalArgumentException("Success and failure reason disagree");
    }

    public static AbilityResult success(String abilityId, AbilityContribution contribution) {
        return new AbilityResult(true, abilityId, AbilityFailureReason.NONE, contribution, false);
    }

    public static AbilityResult scheduled(String abilityId, AbilityContribution contribution) {
        return new AbilityResult(true, abilityId, AbilityFailureReason.NONE, contribution, true);
    }

    public static AbilityResult failure(String abilityId, AbilityFailureReason reason) {
        if (reason == AbilityFailureReason.NONE) throw new IllegalArgumentException("Failure requires a reason");
        return new AbilityResult(false, abilityId, reason, AbilityContribution.NONE, false);
    }
}
