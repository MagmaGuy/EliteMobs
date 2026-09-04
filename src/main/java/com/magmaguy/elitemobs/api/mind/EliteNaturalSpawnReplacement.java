package com.magmaguy.elitemobs.api.mind;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;

import java.util.Objects;
import java.util.function.Consumer;

/** Exact native Mind request plus an owner callback run after EliteMobs commits the actor. */
public record EliteNaturalSpawnReplacement(
        EliteMindSpawnRequest request,
        Consumer<EliteEntity> afterSpawn) implements EliteNaturalSpawnClaim {

    public EliteNaturalSpawnReplacement {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(afterSpawn, "afterSpawn");
    }

    public EliteNaturalSpawnReplacement(EliteMindSpawnRequest request) {
        this(request, ignored -> { });
    }
}
