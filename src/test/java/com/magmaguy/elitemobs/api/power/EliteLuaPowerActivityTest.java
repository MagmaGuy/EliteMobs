package com.magmaguy.elitemobs.api.power;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EliteLuaPowerActivityTest {

    @Test
    void activationRequiresARealSuccessfulHookOrNonRejectedMindAction() {
        NamespacedKey key = NamespacedKey.fromString("ragnablock:power/fire_volley");
        assertFalse(activity(key, Map.of(), 0, 0, 4).activated());
        assertTrue(activity(key, Map.of("on_spawn", 1L), 0, 0, 0).activated());
        assertTrue(activity(key, Map.of(), 1, 0, 0).activated());
        assertTrue(activity(key, Map.of(), 0, 1, 0).activated());
    }

    @Test
    void snapshotsDefensivelyCopyAndRejectInvalidCounters() {
        NamespacedKey key = NamespacedKey.fromString("ragnablock:power/fire_volley");
        LinkedHashMap<String, Long> mutable = new LinkedHashMap<>();
        mutable.put("on_spawn", 1L);
        EliteLuaPowerActivity activity = activity(key, mutable, 0, 0, 0);
        mutable.put("on_death", 1L);
        assertFalse(activity.successfulEventHooks().containsKey("on_death"));
        assertThrows(IllegalArgumentException.class, () ->
                activity(key, Map.of("on_spawn", -1L), 0, 0, 0));
    }

    private static EliteLuaPowerActivity activity(
            NamespacedKey key,
            Map<String, Long> events,
            long accepted,
            long deferred,
            long rejected) {
        return new EliteLuaPowerActivity(
                key,
                1,
                events,
                Map.of(),
                accepted,
                deferred,
                rejected,
                true);
    }
}
