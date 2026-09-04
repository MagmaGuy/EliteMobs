package com.magmaguy.elitemobs.api.mind;

import org.bukkit.NamespacedKey;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** Classloader-safe catalog descriptor for one Lua Mind module revision. */
public record EliteMindModule(
        NamespacedKey key,
        long revision,
        List<NamespacedKey> dependencies) {

    public EliteMindModule {
        Objects.requireNonNull(key, "key");
        if (revision < 1L) throw new IllegalArgumentException("revision must be positive");
        dependencies = List.copyOf(Objects.requireNonNull(dependencies, "dependencies"));
        if (new HashSet<>(dependencies).size() != dependencies.size()) {
            throw new IllegalArgumentException("Mind module dependencies cannot repeat");
        }
        for (NamespacedKey dependency : dependencies) {
            Objects.requireNonNull(dependency, "dependency");
        }
    }
}
