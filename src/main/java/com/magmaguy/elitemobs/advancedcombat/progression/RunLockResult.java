package com.magmaguy.elitemobs.advancedcombat.progression;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Result of freezing the current form and input layout for an instance run. */
public record RunLockResult(Status status, UUID runId, RunSelection selection) {

    public RunLockResult {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(runId, "runId");
        if ((status == Status.LOCKED || status == Status.ALREADY_LOCKED
                || status == Status.LOCKED_BY_ANOTHER_RUN) != (selection != null))
            throw new IllegalArgumentException("Only lock-bearing results include a selection");
    }

    public enum Status {
        LOCKED,
        ALREADY_LOCKED,
        LOCKED_BY_ANOTHER_RUN,
        NOT_READY,
        NO_SELECTED_FORM,
        FORM_LOCKED
    }

    public boolean locked() {
        return status == Status.LOCKED || status == Status.ALREADY_LOCKED;
    }

    public Optional<RunSelection> optionalSelection() {
        return Optional.ofNullable(selection);
    }
}
