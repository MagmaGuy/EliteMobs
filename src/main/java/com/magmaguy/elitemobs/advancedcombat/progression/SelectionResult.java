package com.magmaguy.elitemobs.advancedcombat.progression;

import java.util.Optional;

/** Result of changing one persisted profile selection. */
public record SelectionResult(Status status, ProfileSnapshot snapshot) {

    public enum Status {
        APPLIED,
        UNCHANGED,
        NOT_READY,
        UNKNOWN_FORM,
        LOCKED_FORM
    }

    public boolean accepted() {
        return status == Status.APPLIED || status == Status.UNCHANGED;
    }

    public Optional<ProfileSnapshot> optionalSnapshot() {
        return Optional.ofNullable(snapshot);
    }
}
