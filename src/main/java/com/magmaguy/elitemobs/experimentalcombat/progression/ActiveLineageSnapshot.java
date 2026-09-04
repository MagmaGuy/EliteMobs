package com.magmaguy.elitemobs.experimentalcombat.progression;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Active root-to-form lineage and each ancestor's contribution level. */
public record ActiveLineageSnapshot(
        String activeFormId,
        int activeLocalLevel,
        int activeEffectiveLevel,
        List<String> formIds,
        Map<String, Integer> contributionLevels) {

    public ActiveLineageSnapshot {
        Objects.requireNonNull(activeFormId, "activeFormId");
        formIds = List.copyOf(formIds);
        contributionLevels = Collections.unmodifiableMap(new LinkedHashMap<>(contributionLevels));
    }
}
