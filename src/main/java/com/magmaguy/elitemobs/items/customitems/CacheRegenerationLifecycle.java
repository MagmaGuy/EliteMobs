package com.magmaguy.elitemobs.items.customitems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Coordinates an asynchronous cache rebuild with plugin shutdown.
 *
 * <p>The fair lock is intentionally held only for one rebuild step at a time.
 * Shutdown therefore waits for at most the item currently being constructed,
 * then invalidates the generation before another item can start.</p>
 */
final class CacheRegenerationLifecycle {

    private final ReentrantLock lifecycleLock = new ReentrantLock(true);
    private long generation;

    <T> Attempt<T> beginIf(BooleanSupplier allowed,
                           Supplier<? extends Iterable<T>> snapshotSource) {
        lifecycleLock.lock();
        try {
            if (!allowed.getAsBoolean()) return null;

            List<T> snapshot = new ArrayList<>();
            for (T value : snapshotSource.get())
                snapshot.add(value);

            return new Attempt<>(++generation, List.copyOf(snapshot));
        } finally {
            lifecycleLock.unlock();
        }
    }

    boolean runIfCurrent(long expectedGeneration, Runnable action) {
        lifecycleLock.lock();
        try {
            if (generation != expectedGeneration) return false;
            action.run();
            return true;
        } finally {
            lifecycleLock.unlock();
        }
    }

    boolean isCurrent(long expectedGeneration) {
        lifecycleLock.lock();
        try {
            return generation == expectedGeneration;
        } finally {
            lifecycleLock.unlock();
        }
    }

    void cancel() {
        lifecycleLock.lock();
        try {
            generation++;
        } finally {
            lifecycleLock.unlock();
        }
    }

    record Attempt<T>(long generation, List<T> snapshot) {
    }
}
