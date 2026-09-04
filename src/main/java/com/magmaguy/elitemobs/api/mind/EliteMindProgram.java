package com.magmaguy.elitemobs.api.mind;

import org.bukkit.NamespacedKey;

import java.util.List;
import java.util.Objects;

/**
 * Catalog identity returned after EliteMobs validates a Lua Mind program and snapshots its
 * dependency-first module composition.
 */
public record EliteMindProgram(
        NamespacedKey key,
        long revision,
        List<EliteMindModule> resolvedModules,
        String compositionFingerprint) {

    /** Source-compatible descriptor for callers that do not retain composition metadata. */
    public EliteMindProgram(NamespacedKey key, long revision) {
        this(key, revision, List.of(), "");
    }

    public EliteMindProgram {
        Objects.requireNonNull(key, "key");
        if (revision < 1L) throw new IllegalArgumentException("revision must be positive");
        resolvedModules = List.copyOf(Objects.requireNonNull(resolvedModules, "resolvedModules"));
        compositionFingerprint = Objects.requireNonNull(
                compositionFingerprint, "compositionFingerprint");
    }
}
