package com.magmaguy.elitemobs.advancedcombat.constructs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Pure ownership index for overlapping packet-only constructs.
 *
 * <p>Each construct keeps its original stacking priority while it moves. Replacing or removing a
 * construct reports only changes to the layer a viewer can actually see, so an obscured visual
 * never causes a false block restore packet.</p>
 */
final class PacketConstructLayerIndex<K, V> {
    private final Map<UUID, Layer<K, V>> layers = new LinkedHashMap<>();
    private long nextPriority;

    List<VisibilityChange<K, V>> replace(UUID constructId, Map<K, V> values) {
        Objects.requireNonNull(constructId, "constructId");
        Objects.requireNonNull(values, "values");
        LinkedHashMap<K, V> replacement = new LinkedHashMap<>();
        values.forEach((key, value) -> replacement.put(
                Objects.requireNonNull(key, "construct key"),
                Objects.requireNonNull(value, "construct value")));
        if (replacement.isEmpty()) return remove(constructId);

        Layer<K, V> previous = layers.get(constructId);
        Set<K> affected = new LinkedHashSet<>();
        if (previous != null) affected.addAll(previous.values().keySet());
        affected.addAll(replacement.keySet());
        Map<K, Optional<V>> before = snapshot(affected);

        long priority = previous == null ? ++nextPriority : previous.priority();
        layers.put(constructId, new Layer<>(priority, Map.copyOf(replacement)));
        return changes(affected, before);
    }

    List<VisibilityChange<K, V>> remove(UUID constructId) {
        Objects.requireNonNull(constructId, "constructId");
        Layer<K, V> existing = layers.get(constructId);
        if (existing == null) return List.of();

        Set<K> affected = new LinkedHashSet<>(existing.values().keySet());
        Map<K, Optional<V>> before = snapshot(affected);
        layers.remove(constructId);
        return changes(affected, before);
    }

    Optional<V> visible(K key) {
        Objects.requireNonNull(key, "key");
        Layer<K, V> highest = null;
        for (Layer<K, V> layer : layers.values()) {
            if (!layer.values().containsKey(key)) continue;
            if (highest == null || layer.priority() > highest.priority()) highest = layer;
        }
        return highest == null ? Optional.empty() : Optional.of(highest.values().get(key));
    }

    boolean isEmpty() {
        return layers.isEmpty();
    }

    private Map<K, Optional<V>> snapshot(Set<K> keys) {
        Map<K, Optional<V>> snapshot = new LinkedHashMap<>();
        for (K key : keys) snapshot.put(key, visible(key));
        return snapshot;
    }

    private List<VisibilityChange<K, V>> changes(
            Set<K> affected,
            Map<K, Optional<V>> before) {
        List<VisibilityChange<K, V>> changes = new ArrayList<>();
        for (K key : affected) {
            Optional<V> after = visible(key);
            Optional<V> prior = before.getOrDefault(key, Optional.empty());
            if (!prior.equals(after)) changes.add(new VisibilityChange<>(key, prior, after));
        }
        return List.copyOf(changes);
    }

    record VisibilityChange<K, V>(K key, Optional<V> before, Optional<V> after) {
        VisibilityChange {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(before, "before");
            Objects.requireNonNull(after, "after");
        }
    }

    private record Layer<K, V>(long priority, Map<K, V> values) {
    }
}
