package com.magmaguy.elitemobs.api.mind;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Objects;

/** Read-only facts exposed while EliteMobs decides a natural creature spawn. */
public record EliteNaturalSpawnContext(
        LivingEntity source,
        CreatureSpawnEvent.SpawnReason spawnReason,
        int suggestedLevel) {

    public EliteNaturalSpawnContext {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(spawnReason, "spawnReason");
        if (suggestedLevel < 0) {
            throw new IllegalArgumentException("suggestedLevel cannot be negative");
        }
    }
}
