package com.magmaguy.elitemobs.skills.bonuses;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rolls weapon skill procs. The combat diagnostic can force a skill's outcome for its own player so it
 * can check what the proc does without waiting for the dice; every other roll is random.
 */
public final class ProcRoll {
    private static final Map<UUID, Map<String, Boolean>> forced = new ConcurrentHashMap<>();

    private ProcRoll() {
    }

    public static boolean rolls(Player player, String skillId, double chance) {
        Map<String, Boolean> outcomes = forced.get(player.getUniqueId());
        Boolean outcome = outcomes == null ? null : outcomes.get(skillId);
        return outcome != null ? outcome : ThreadLocalRandom.current().nextDouble() < chance;
    }

    /** Forces a skill's proc for one player; {@code null} returns it to random rolls. */
    public static void force(UUID playerId, String skillId, Boolean outcome) {
        if (outcome == null) {
            forced.computeIfPresent(playerId, (id, outcomes) -> {
                outcomes.remove(skillId);
                return outcomes.isEmpty() ? null : outcomes;
            });
            return;
        }
        forced.computeIfAbsent(playerId, id -> new ConcurrentHashMap<>()).put(skillId, outcome);
    }

    public static void clear(UUID playerId) {
        forced.remove(playerId);
    }
}
