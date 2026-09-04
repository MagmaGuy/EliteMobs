package com.magmaguy.elitemobs.experimentalcombat.abilities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** One timed actor-owned suppression lease per affected Elite. */
final class TimedSuppressionLeaseRegistry<K> implements AutoCloseable {
    private final Map<K, Entry> entries = new LinkedHashMap<>();

    boolean apply(K target, long expiresAtTick, Supplier<? extends AutoCloseable> factory) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(factory, "factory");
        Entry current = entries.get(target);
        if (current != null) {
            current.expiresAtTick = Math.max(current.expiresAtTick, expiresAtTick);
            return true;
        }

        AutoCloseable lease;
        try {
            lease = factory.get();
        } catch (RuntimeException unsupported) {
            return false;
        }
        if (lease == null) return false;
        entries.put(target, new Entry(expiresAtTick, lease));
        return true;
    }

    void maintain(long currentTick, Predicate<K> targetValid) {
        Objects.requireNonNull(targetValid, "targetValid");
        for (K target : new ArrayList<>(entries.keySet())) {
            Entry entry = entries.get(target);
            if (entry != null
                    && (currentTick >= entry.expiresAtTick || !targetValid.test(target))) {
                remove(target, entry);
            }
        }
    }

    void remove(K target) {
        Entry entry = entries.get(target);
        if (entry != null) remove(target, entry);
    }

    int size() {
        return entries.size();
    }

    @Override
    public void close() {
        for (Entry entry : new ArrayList<>(entries.values())) closeQuietly(entry.lease);
        entries.clear();
    }

    private void remove(K target, Entry expected) {
        if (!entries.remove(target, expected)) return;
        closeQuietly(expected.lease);
    }

    private static void closeQuietly(AutoCloseable lease) {
        try {
            lease.close();
        } catch (Exception ignored) {
            // Ownership is already removed. A broken lease cannot poison the shared tick task.
        }
    }

    private static final class Entry {
        private long expiresAtTick;
        private final AutoCloseable lease;

        private Entry(long expiresAtTick, AutoCloseable lease) {
            this.expiresAtTick = expiresAtTick;
            this.lease = lease;
        }
    }
}
