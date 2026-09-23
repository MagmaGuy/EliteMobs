package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Server-thread owner of reversible script and skill mutations and their expiry tasks. */
public final class TimedScriptStateManager {
    private static final Map<StateKey, State> states = new HashMap<>();
    private static final Map<Object, Set<StateKey>> ownerKeys = new IdentityHashMap<>();

    private TimedScriptStateManager() {}

    static <T> void apply(UUID targetId, String property, T value, int durationTicks,
                          Supplier<T> currentValue, Consumer<T> writer) {
        StateKey key = new StateKey(targetId, property);
        if (durationTicks <= 0) {
            writer.accept(value);
            discard(key, states.get(key));
        } else applyOwned(new Object(), targetId, property, value, durationTicks, currentValue, writer);
    }

    /** Replaces this owner's contribution; a zero duration lasts until releaseOwner. */
    public static <T> void applyOwned(Object owner, UUID targetId, String property, T value, int durationTicks,
                                      Supplier<T> currentValue, Consumer<T> writer) {
        Objects.requireNonNull(owner, "owner");
        StateKey key = new StateKey(targetId, property);
        State state = states.get(key);
        T current = currentValue.get();
        // An outside writer superseded our last value. It now owns the baseline.
        if (state != null && !Objects.equals(current, state.appliedValue)) {
            discard(key, state);
            state = null;
        }
        State selected = state == null ? new State(current) : state;
        Mutation mutation = new Mutation(owner, value);
        if (durationTicks > 0)
            mutation.task = MetadataHandler.PLUGIN.getServer().getScheduler().runTaskLater(
                    MetadataHandler.PLUGIN, () -> release(key, selected, mutation), durationTicks);
        try {
            writer.accept(value);
        } catch (RuntimeException | Error failure) {
            if (mutation.task != null) mutation.task.cancel();
            throw failure;
        }
        Mutation previous = selected.active.remove(owner);
        if (previous != null && previous.task != null) previous.task.cancel();
        selected.active.put(owner, mutation);
        selected.reader = currentValue::get;
        selected.writer = raw -> writer.accept(cast(raw));
        selected.appliedValue = value;
        states.put(key, selected);
        ownerKeys.computeIfAbsent(owner, ignored -> new HashSet<>()).add(key);
    }

    public static void applyInvulnerability(Object owner, LivingEntity target, boolean value, int durationTicks) {
        UUID id = target.getUniqueId();
        applyOwned(owner, id, "invulnerable", new Invulnerability(value, target instanceof Player && value),
                durationTicks, () -> new Invulnerability(target.isInvulnerable(),
                        target instanceof Player && ScriptAction.getInvulnerablePlayers().contains(id)),
                state -> {
                    target.setInvulnerable(state.nativeValue());
                    if (target instanceof Player) {
                        if (state.scriptOwned()) ScriptAction.getInvulnerablePlayers().add(id);
                        else ScriptAction.getInvulnerablePlayers().remove(id);
                    }
                });
    }

    public static boolean releaseProperty(Object owner, UUID targetId, String property) {
        StateKey key = new StateKey(targetId, property);
        State state = states.get(key);
        if (state == null || !state.active.containsKey(owner)) return false;
        release(key, state, state.active.get(owner));
        return true;
    }

    public static void releaseOwner(Object owner) {
        Set<StateKey> keys = ownerKeys.get(owner);
        if (keys == null) return;
        for (StateKey key : Set.copyOf(keys)) {
            State state = states.get(key);
            if (state != null) release(key, state, state.active.get(owner));
        }
    }

    static void shutdown() {
        for (Map.Entry<StateKey, State> entry : new ArrayList<>(states.entrySet())) {
            State state = entry.getValue();
            if (restore(entry.getKey(), state, state.baseline)) discard(entry.getKey(), state);
        }
    }

    private static void release(StateKey key, State state, Mutation mutation) {
        if (mutation == null || states.get(key) != state || state.active.get(mutation.owner) != mutation) return;
        Object next = state.baseline;
        for (Mutation candidate : state.active.values()) if (candidate != mutation) next = candidate.value;
        if (!restore(key, state, next)) return;
        if (states.get(key) != state) return;
        state.active.remove(mutation.owner);
        if (mutation.task != null) mutation.task.cancel();
        unindex(mutation.owner, key);
        if (state.active.isEmpty()) states.remove(key, state);
    }

    private static boolean restore(StateKey key, State state, Object value) {
        try {
            if (!Objects.equals(state.reader.get(), state.appliedValue)) {
                discard(key, state);
                return true;
            }
            state.writer.accept(value);
            state.appliedValue = value;
            return true;
        } catch (RuntimeException failure) {
            Logger.warn("Could not restore timed state " + key.property + ": " + failure.getMessage());
            return false; // Retain ownership for lifecycle cleanup to retry.
        }
    }

    private static void discard(StateKey key, State state) {
        if (state == null || !states.remove(key, state)) return;
        for (Mutation mutation : state.active.values()) {
            if (mutation.task != null) mutation.task.cancel();
            unindex(mutation.owner, key);
        }
    }

    private static void unindex(Object owner, StateKey key) {
        Set<StateKey> keys = ownerKeys.get(owner);
        if (keys == null) return;
        keys.remove(key);
        if (keys.isEmpty()) ownerKeys.remove(owner);
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object value) { return (T) value; }
    private record StateKey(UUID targetId, String property) {}
    private record Invulnerability(boolean nativeValue, boolean scriptOwned) {}
    private static final class Mutation {
        final Object owner;
        final Object value;
        BukkitTask task;
        Mutation(Object owner, Object value) { this.owner = owner; this.value = value; }
    }
    private static final class State {
        final Object baseline;
        final Map<Object, Mutation> active = new LinkedHashMap<>();
        Supplier<Object> reader;
        Consumer<Object> writer;
        Object appliedValue;
        State(Object baseline) { this.baseline = baseline; }
    }
}
