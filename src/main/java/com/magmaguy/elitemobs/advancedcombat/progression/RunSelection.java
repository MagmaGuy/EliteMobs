package com.magmaguy.elitemobs.advancedcombat.progression;

import java.util.Objects;

/** Immutable, memory-only form and input selection locked for one instance run. */
public record RunSelection(String formId, InputProfile inputProfile) {

    public RunSelection {
        Objects.requireNonNull(formId, "formId");
        Objects.requireNonNull(inputProfile, "inputProfile");
        if (formId.isBlank()) throw new IllegalArgumentException("formId must not be blank");
    }
}
