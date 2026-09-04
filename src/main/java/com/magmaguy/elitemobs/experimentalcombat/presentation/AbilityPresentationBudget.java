package com.magmaguy.elitemobs.experimentalcombat.presentation;

/** Hard work limits for one committed ability cast. */
public record AbilityPresentationBudget(
        int particleLimit,
        int soundLimit,
        int targetBurstLimit,
        int traceSampleLimit) {

    public static final AbilityPresentationBudget STANDARD =
            new AbilityPresentationBudget(160, 8, 8, 18);

    public AbilityPresentationBudget {
        if (particleLimit < 1 || soundLimit < 1 || targetBurstLimit < 1 || traceSampleLimit < 1)
            throw new IllegalArgumentException("Presentation limits must be positive");
    }
}
