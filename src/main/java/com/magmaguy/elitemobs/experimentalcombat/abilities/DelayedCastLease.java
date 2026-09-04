package com.magmaguy.elitemobs.experimentalcombat.abilities;

import java.util.Objects;
import java.util.UUID;

/** Pure ownership token shared by every delayed class-ability callback. */
record DelayedCastLease(UUID casterId, long lifecycleGeneration) {
    DelayedCastLease {
        Objects.requireNonNull(casterId, "casterId");
    }

    boolean permits(UUID currentCasterId, long currentGeneration, boolean runtimeEligible) {
        return runtimeEligible
                && casterId.equals(currentCasterId)
                && lifecycleGeneration == currentGeneration;
    }
}
