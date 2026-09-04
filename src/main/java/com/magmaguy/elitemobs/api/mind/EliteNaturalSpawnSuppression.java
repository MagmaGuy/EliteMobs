package com.magmaguy.elitemobs.api.mind;

import java.util.Objects;

/** A claimed carrier that must not survive as vanilla or enter generic EliteMobs conversion. */
public record EliteNaturalSpawnSuppression(String reason) implements EliteNaturalSpawnClaim {
    public EliteNaturalSpawnSuppression {
        Objects.requireNonNull(reason, "reason");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("Natural spawn suppression needs a reason");
        }
    }
}
