package com.magmaguy.elitemobs.api.mind;

import org.bukkit.NamespacedKey;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Classloader-safe view of one native mind runtime. */
public record EliteMindSnapshot(
        UUID eliteId,
        NamespacedKey programKey,
        long programRevision,
        long generation,
        Set<String> activeBehaviors,
        boolean bodyAttached,
        boolean paused,
        List<EliteMindModule> resolvedModules,
        String compositionFingerprint) {

    /** Source-compatible runtime view for callers that do not retain composition metadata. */
    public EliteMindSnapshot(
            UUID eliteId,
            NamespacedKey programKey,
            long programRevision,
            long generation,
            Set<String> activeBehaviors,
            boolean bodyAttached) {
        this(
                eliteId,
                programKey,
                programRevision,
                generation,
                activeBehaviors,
                bodyAttached,
                false,
                List.of(),
                "");
    }

    public EliteMindSnapshot {
        Objects.requireNonNull(eliteId, "eliteId");
        Objects.requireNonNull(programKey, "programKey");
        Objects.requireNonNull(activeBehaviors, "activeBehaviors");
        if (programRevision < 1L) throw new IllegalArgumentException("programRevision must be positive");
        if (generation < 0L) throw new IllegalArgumentException("generation cannot be negative");
        activeBehaviors = Collections.unmodifiableSet(new LinkedHashSet<>(activeBehaviors));
        resolvedModules = List.copyOf(Objects.requireNonNull(resolvedModules, "resolvedModules"));
        compositionFingerprint = Objects.requireNonNull(
                compositionFingerprint, "compositionFingerprint");
    }
}
