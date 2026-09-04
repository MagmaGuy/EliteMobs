package com.magmaguy.elitemobs.experimentalcombat.minions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/** Deterministic owner roster whose resummon policy replaces the oldest minion first. */
public final class ClassMinionRoster<T> {
    private final int cap;
    private final Deque<T> entries = new ArrayDeque<>();

    public ClassMinionRoster(int cap) {
        if (cap < 1) throw new IllegalArgumentException("cap must be positive");
        this.cap = cap;
    }

    public List<T> admit(T entry) {
        return admitAll(List.of(Objects.requireNonNull(entry, "entry")));
    }

    public List<T> admitAll(Collection<? extends T> additions) {
        Objects.requireNonNull(additions, "additions");
        if (additions.size() > cap) throw new IllegalArgumentException("A batch cannot exceed the roster cap");
        List<T> evicted = new ArrayList<>();
        for (T addition : additions) {
            Objects.requireNonNull(addition, "addition");
            while (entries.size() >= cap) evicted.add(entries.removeFirst());
            entries.addLast(addition);
        }
        return List.copyOf(evicted);
    }

    public boolean remove(T entry) {
        return entries.remove(entry);
    }

    public List<T> entries() {
        return List.copyOf(entries);
    }

    public List<T> drain() {
        List<T> drained = entries();
        entries.clear();
        return drained;
    }
}
