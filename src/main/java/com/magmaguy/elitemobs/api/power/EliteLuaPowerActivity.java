package com.magmaguy.elitemobs.api.power;

import org.bukkit.NamespacedKey;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable activity observed from one attached Lua power runtime.
 *
 * <p>Hook counters advance only when the canonical EliteMobs runtime actually dispatches that
 * hook. Event hooks count as successful only when the MagmaCore script instance remains healthy
 * after execution. Mind-action results preserve the Lua program's accepted/deferred/rejected
 * response; a rejected action is a dispatch observation, not a power activation.</p>
 */
public record EliteLuaPowerActivity(
        NamespacedKey key,
        long revision,
        Map<String, Long> successfulEventHooks,
        Map<String, Long> failedEventHooks,
        long acceptedMindActions,
        long deferredMindActions,
        long rejectedMindActions,
        boolean runtimeActive) {

    public EliteLuaPowerActivity {
        Objects.requireNonNull(key, "key");
        if (revision < 1L) throw new IllegalArgumentException("revision must be positive");
        successfulEventHooks = immutableCounters(successfulEventHooks, "successfulEventHooks");
        failedEventHooks = immutableCounters(failedEventHooks, "failedEventHooks");
        requireNonNegative(acceptedMindActions, "acceptedMindActions");
        requireNonNegative(deferredMindActions, "deferredMindActions");
        requireNonNegative(rejectedMindActions, "rejectedMindActions");
    }

    /** Returns true after at least one canonical event hook or non-rejected Mind action ran. */
    public boolean activated() {
        return !successfulEventHooks.isEmpty()
                || acceptedMindActions > 0L
                || deferredMindActions > 0L;
    }

    private static Map<String, Long> immutableCounters(
            Map<String, Long> values,
            String label) {
        Objects.requireNonNull(values, label);
        LinkedHashMap<String, Long> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : values.entrySet()) {
            String hook = Objects.requireNonNull(entry.getKey(), label + " hook");
            if (hook.isBlank()) throw new IllegalArgumentException(label + " contains a blank hook");
            long count = Objects.requireNonNull(entry.getValue(), label + " count");
            requireNonNegative(count, label + '[' + hook + ']');
            if (count > 0L) copy.put(hook, count);
        }
        return Collections.unmodifiableMap(copy);
    }

    private static void requireNonNegative(long value, String label) {
        if (value < 0L) throw new IllegalArgumentException(label + " cannot be negative");
    }
}
