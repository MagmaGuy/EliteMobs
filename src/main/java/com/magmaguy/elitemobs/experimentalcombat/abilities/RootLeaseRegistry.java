package com.magmaguy.elitemobs.experimentalcombat.abilities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Owns one suppression lease per target and independently expiring roots per caster. */
final class RootLeaseRegistry<K, S> implements AutoCloseable {
    private final Map<K, Entry<S>> entries = new LinkedHashMap<>();

    boolean apply(K target, S source, long expiresAtTick,
                  Supplier<? extends AutoCloseable> suppressionFactory) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(suppressionFactory, "suppressionFactory");
        Entry<S> current = entries.get(target);
        if (current != null) {
            current.expiryBySource.merge(source, expiresAtTick, Math::max);
            return true;
        }
        AutoCloseable suppression;
        try {
            suppression = suppressionFactory.get();
        } catch (RuntimeException unsupported) {
            return false;
        }
        if (suppression == null) return false;
        Entry<S> created = new Entry<>(suppression);
        created.expiryBySource.put(source, expiresAtTick);
        entries.put(target, created);
        return true;
    }

    boolean extend(K target, S source, long additionalTicks, long currentTick) {
        if (additionalTicks <= 0L) return false;
        Entry<S> entry = entries.get(target);
        if (entry == null) return false;
        Long current = entry.expiryBySource.get(source);
        if (current == null || current <= currentTick) return false;
        long base = Math.max(currentTick, current);
        entry.expiryBySource.put(source, saturatedAdd(base, additionalTicks));
        return true;
    }

    void maintain(long currentTick, Predicate<K> targetValid) {
        Objects.requireNonNull(targetValid, "targetValid");
        for (K target : new ArrayList<>(entries.keySet())) {
            Entry<S> entry = entries.get(target);
            if (entry == null) continue;
            entry.expiryBySource.entrySet().removeIf(item -> item.getValue() <= currentTick);
            if (entry.expiryBySource.isEmpty() || !targetValid.test(target)) remove(target, entry);
        }
    }

    Set<K> activeTargets() {
        return Set.copyOf(new LinkedHashSet<>(entries.keySet()));
    }

    boolean ownedBy(K target, S source) {
        Entry<S> entry = entries.get(target);
        return entry != null && entry.expiryBySource.containsKey(source);
    }

    void remove(K target) {
        Entry<S> entry = entries.get(target);
        if (entry != null) remove(target, entry);
    }

    @Override
    public void close() {
        for (Entry<S> entry : new ArrayList<>(entries.values())) closeQuietly(entry.suppression);
        entries.clear();
    }

    private void remove(K target, Entry<S> expected) {
        if (!entries.remove(target, expected)) return;
        closeQuietly(expected.suppression);
    }

    private static void closeQuietly(AutoCloseable lease) {
        try {
            lease.close();
        } catch (Exception ignored) {
        }
    }

    private static long saturatedAdd(long value, long increment) {
        return value > Long.MAX_VALUE - increment ? Long.MAX_VALUE : value + increment;
    }

    private static final class Entry<S> {
        private final Map<S, Long> expiryBySource = new LinkedHashMap<>();
        private final AutoCloseable suppression;

        private Entry(AutoCloseable suppression) {
            this.suppression = suppression;
        }
    }
}
