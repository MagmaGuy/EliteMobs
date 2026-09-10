package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Pure lifecycle policy for one redirectable Fear movement lease per target. */
final class FearLeaseRegistry<K, S> implements AutoCloseable {
    private final Map<K, Entry<S>> entries = new LinkedHashMap<>();

    boolean apply(
            K target,
            UUID source,
            S sourceState,
            long expiresAtTick,
            Supplier<Optional<Redirectable<S>>> factory) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(sourceState, "sourceState");
        Objects.requireNonNull(factory, "factory");

        Entry<S> current = entries.get(target);
        if (current != null) {
            if (isActive(current.handle) && retarget(current.handle, sourceState)) {
                current.source = source;
                current.expiresAtTick = Math.max(current.expiresAtTick, expiresAtTick);
                return true;
            }
            remove(target, current);
        }

        Optional<Redirectable<S>> opened;
        try {
            opened = factory.get();
        } catch (RuntimeException unsupported) {
            return false;
        }
        if (opened == null) return false;
        if (opened.isEmpty()) return false;
        Redirectable<S> handle = opened.get();
        if (!isActive(handle) || !retarget(handle, sourceState)) {
            closeQuietly(handle);
            return false;
        }
        entries.put(target, new Entry<>(source, expiresAtTick, handle));
        return true;
    }

    void maintain(
            long currentTick,
            Predicate<K> targetValid,
            Function<UUID, Optional<S>> sourceResolver) {
        Objects.requireNonNull(targetValid, "targetValid");
        Objects.requireNonNull(sourceResolver, "sourceResolver");
        for (K target : new ArrayList<>(entries.keySet())) {
            Entry<S> entry = entries.get(target);
            if (entry == null) continue;
            if (currentTick >= entry.expiresAtTick
                    || !targetValid.test(target)) {
                remove(target, entry);
                continue;
            }
            Optional<S> sourceState = sourceResolver.apply(entry.source);
            if (sourceState.isEmpty()
                    || !isActive(entry.handle)
                    || !retarget(entry.handle, sourceState.get())) {
                remove(target, entry);
            }
        }
    }

    void clearSource(UUID source) {
        for (K target : new ArrayList<>(entries.keySet())) {
            Entry<S> entry = entries.get(target);
            if (entry.source.equals(source)) remove(target, entry);
        }
    }

    void remove(K target) {
        Entry<S> entry = entries.get(target);
        if (entry != null) remove(target, entry);
    }

    int size() {
        return entries.size();
    }

    boolean ownedBy(K target, UUID source) {
        Entry<S> entry = entries.get(target);
        return entry != null && entry.source.equals(source) && isActive(entry.handle);
    }

    @Override
    public void close() {
        for (Entry<S> entry : new ArrayList<>(entries.values())) closeQuietly(entry.handle);
        entries.clear();
    }

    private void remove(K target, Entry<S> expected) {
        if (!entries.remove(target, expected)) return;
        closeQuietly(expected.handle);
    }

    private static <S> boolean isActive(Redirectable<S> handle) {
        try {
            return handle.isActive();
        } catch (RuntimeException unsupported) {
            return false;
        }
    }

    private static <S> boolean retarget(Redirectable<S> handle, S sourceState) {
        try {
            return handle.retarget(sourceState);
        } catch (RuntimeException unsupported) {
            return false;
        }
    }

    private static void closeQuietly(Redirectable<?> handle) {
        try {
            handle.close();
        } catch (RuntimeException ignored) {
            // The registry has already relinquished ownership; a broken adapter must not retain
            // the lease or kill the repeating lifecycle task.
        }
    }

    interface Redirectable<S> extends AutoCloseable {
        boolean isActive();

        boolean retarget(S sourceState);

        @Override
        void close();
    }

    private static final class Entry<S> {
        private UUID source;
        private long expiresAtTick;
        private final Redirectable<S> handle;

        private Entry(UUID source, long expiresAtTick, Redirectable<S> handle) {
            this.source = source;
            this.expiresAtTick = expiresAtTick;
            this.handle = handle;
        }
    }
}
