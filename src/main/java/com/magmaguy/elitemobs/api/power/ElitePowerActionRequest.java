package com.magmaguy.elitemobs.api.power;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.NamespacedKey;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Classloader-safe semantic action request admitted by a power attached to an EliteMobs actor.
 * Payload values are limited to immutable JDK values and {@link ElitePowerActionPosition}.
 */
public record ElitePowerActionRequest(
        EliteEntity actor,
        NamespacedKey actionKey,
        Map<String, Object> payload,
        long gameTick,
        long generation) {

    public ElitePowerActionRequest {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(actionKey, "actionKey");
        Objects.requireNonNull(payload, "payload");
        if (generation < 1L) throw new IllegalArgumentException("generation must be positive");
        LinkedHashMap<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = Objects.requireNonNull(entry.getKey(), "payload key");
            Object value = Objects.requireNonNull(entry.getValue(), "payload value for " + key);
            if (!(value instanceof String)
                    && !(value instanceof Boolean)
                    && !(value instanceof Long)
                    && !(value instanceof Double)
                    && !(value instanceof UUID)
                    && !(value instanceof ElitePowerActionPosition)) {
                throw new IllegalArgumentException(
                        "Unsupported power action payload type for " + key + ": "
                                + value.getClass().getName());
            }
            copy.put(key, value);
        }
        payload = Collections.unmodifiableMap(copy);
    }
}
