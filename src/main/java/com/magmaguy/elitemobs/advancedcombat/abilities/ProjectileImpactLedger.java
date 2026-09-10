package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Single-owner ledger for physical ability projectiles. Native entity damage, Bukkit projectile
 * hits and FMM OBB hits can all report the same collision. Removing the entry before impact work
 * starts makes exactly one of those routes authoritative.
 */
final class ProjectileImpactLedger<T> {
    private final Map<UUID, Entry<T>> entries = new LinkedHashMap<>();

    boolean register(UUID projectileId, UUID casterId, T payload) {
        if (projectileId == null || casterId == null || payload == null) return false;
        return entries.putIfAbsent(projectileId, new Entry<>(casterId, payload)) == null;
    }

    Optional<T> claim(UUID projectileId) {
        return retire(projectileId);
    }

    Optional<T> retire(UUID projectileId) {
        Entry<T> removed = entries.remove(projectileId);
        return removed == null ? Optional.empty() : Optional.of(removed.payload());
    }

    List<T> retireCaster(UUID casterId) {
        List<T> retired = new ArrayList<>();
        entries.entrySet().removeIf(entry -> {
            if (!entry.getValue().casterId().equals(casterId)) return false;
            retired.add(entry.getValue().payload());
            return true;
        });
        return List.copyOf(retired);
    }

    List<T> retireAll() {
        List<T> retired = entries.values().stream().map(Entry::payload).toList();
        entries.clear();
        return retired;
    }

    boolean contains(UUID projectileId) {
        return entries.containsKey(projectileId);
    }

    private record Entry<T>(UUID casterId, T payload) {
    }
}
