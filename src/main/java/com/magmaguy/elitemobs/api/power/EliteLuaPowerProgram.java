package com.magmaguy.elitemobs.api.power;

import org.bukkit.NamespacedKey;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Classloader-safe descriptor for a validated EliteMobs Lua power program. Lower execution
 * priorities dispatch first; attachment order is stable only between programs with equal priority.
 */
public record EliteLuaPowerProgram(
        NamespacedKey key,
        long revision,
        int executionPriority,
        Set<String> hooks,
        EliteLuaPowerType type,
        String effect) {

    public EliteLuaPowerProgram {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(hooks, "hooks");
        Objects.requireNonNull(type, "type");
        if (revision < 1L) throw new IllegalArgumentException("revision must be positive");
        hooks = Collections.unmodifiableSet(new LinkedHashSet<>(hooks));
    }
}
