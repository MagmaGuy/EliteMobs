package com.magmaguy.elitemobs.advancedcombat.progression;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Immutable view consumed by menus, commands and combat mechanics. */
public record ProfileSnapshot(
        UUID playerId,
        String selectedFormId,
        InputProfile selectedInputProfile,
        int catalogVersion,
        UUID lockedRunId,
        RunSelection lockedRunSelection,
        ActiveLineageSnapshot activeLineage,
        Map<String, FormProgressSnapshot> forms) {

    public ProfileSnapshot {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(selectedInputProfile, "selectedInputProfile");
        if ((lockedRunId == null) != (lockedRunSelection == null))
            throw new IllegalArgumentException("Run id and run selection must both be present or absent");
        forms = Collections.unmodifiableMap(new LinkedHashMap<>(forms));
    }

    public Optional<String> optionalSelectedFormId() {
        return Optional.ofNullable(selectedFormId);
    }

    public Optional<RunSelection> optionalLockedRunSelection() {
        return Optional.ofNullable(lockedRunSelection);
    }

    public Optional<UUID> optionalLockedRunId() {
        return Optional.ofNullable(lockedRunId);
    }

    public Optional<ActiveLineageSnapshot> optionalActiveLineage() {
        return Optional.ofNullable(activeLineage);
    }

    public Optional<String> activeFormId() {
        if (lockedRunSelection != null) return Optional.of(lockedRunSelection.formId());
        return optionalSelectedFormId();
    }

    public InputProfile activeInputProfile() {
        return lockedRunSelection == null ? selectedInputProfile : lockedRunSelection.inputProfile();
    }
}
