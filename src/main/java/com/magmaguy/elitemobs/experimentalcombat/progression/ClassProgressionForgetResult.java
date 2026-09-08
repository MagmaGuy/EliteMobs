package com.magmaguy.elitemobs.experimentalcombat.progression;

/** Result of resetting one class subtree through the admin debug command. */
public record ClassProgressionForgetResult(Status status, int formsReset, boolean selectionCleared) {
    public enum Status {
        APPLIED,
        NOT_READY,
        UNKNOWN_FORM,
        RUN_LOCKED
    }
}
