package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Owns reversible timed script mutations and their expiry tasks.
 * <p>
 * Each property keeps its original value plus the ordered active mutations. Expiring an older
 * mutation therefore cannot undo a newer one, and the true baseline is restored when the final
 * mutation expires.
 */
final class TimedScriptStateManager {

    private static final Map<StateKey, State> states = new HashMap<>();
    private static long nextToken;

    private TimedScriptStateManager() {
    }

    static <T> void apply(UUID targetId, String property, T value, int durationTicks,
                          Supplier<T> currentValue, Consumer<T> writer) {
        StateKey key = new StateKey(targetId, property);
        if (durationTicks <= 0) {
            cancelState(key, false);
            safeWrite(property, writer, value);
            return;
        }

        State state = states.computeIfAbsent(key, ignored -> new State(currentValue.get()));
        state.writer = rawValue -> writer.accept(cast(rawValue));

        long token = ++nextToken;
        state.activeValues.put(token, value);
        safeWrite(property, writer, value);
        BukkitTask task = MetadataHandler.PLUGIN.getServer().getScheduler().runTaskLater(
                MetadataHandler.PLUGIN, () -> expire(key, token), durationTicks);
        state.tasks.put(token, task);
    }

    static void shutdown() {
        for (Map.Entry<StateKey, State> entry : states.entrySet()) {
            State state = entry.getValue();
            state.tasks.values().forEach(BukkitTask::cancel);
            safeWrite(entry.getKey().property, state.writer, state.baseline);
        }
        states.clear();
    }

    private static void expire(StateKey key, long token) {
        State state = states.get(key);
        if (state == null) return;

        state.tasks.remove(token);
        state.activeValues.remove(token);
        if (state.activeValues.isEmpty()) {
            safeWrite(key.property, state.writer, state.baseline);
            states.remove(key);
            return;
        }

        Object latestValue = null;
        for (Object value : state.activeValues.values()) latestValue = value;
        safeWrite(key.property, state.writer, latestValue);
    }

    private static void cancelState(StateKey key, boolean restoreBaseline) {
        State state = states.remove(key);
        if (state == null) return;
        state.tasks.values().forEach(BukkitTask::cancel);
        if (restoreBaseline) safeWrite(key.property, state.writer, state.baseline);
    }

    private static <T> void safeWrite(String property, Consumer<T> writer, T value) {
        if (writer == null) return;
        try {
            writer.accept(value);
        } catch (RuntimeException exception) {
            Logger.warn("Failed to restore timed script state for " + property + ": " + exception.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object value) {
        return (T) value;
    }

    private record StateKey(UUID targetId, String property) {
    }

    private static final class State {
        private final Object baseline;
        private final Map<Long, Object> activeValues = new LinkedHashMap<>();
        private final Map<Long, BukkitTask> tasks = new HashMap<>();
        private Consumer<Object> writer;

        private State(Object baseline) {
            this.baseline = baseline;
        }
    }
}
