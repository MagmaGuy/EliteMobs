package com.magmaguy.elitemobs.advancedcombat.progression;

import java.util.Objects;
import java.util.UUID;

/**
 * Raw persisted XP for one player and one independently progressing class form.
 * The Module validates identifiers, XP and catalog versions while hydrating the cache.
 */
public record StoredClassProgress(
        UUID playerId,
        String formId,
        long xp,
        int catalogVersion,
        boolean challengeCompleted) {

    public StoredClassProgress(UUID playerId, String formId, long xp, int catalogVersion) {
        this(playerId, formId, xp, catalogVersion, false);
    }

    public StoredClassProgress {
        Objects.requireNonNull(playerId, "playerId");
    }

    public static StoredClassProgress zero(UUID playerId, String formId, int catalogVersion) {
        Objects.requireNonNull(formId, "formId");
        if (formId.isBlank()) throw new IllegalArgumentException("formId must not be blank");
        if (catalogVersion < 0) throw new IllegalArgumentException("catalogVersion must not be negative");
        return new StoredClassProgress(playerId, formId, 0, catalogVersion);
    }
}
