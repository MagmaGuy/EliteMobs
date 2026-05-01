package com.magmaguy.elitemobs.skills.bonuses.interfaces;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player, per-skill internal proc cooldown tracker.
 * <p>
 * Used to enforce {@link ProcSkill#getInternalCooldownMillis()} so burst-damage
 * procs cannot fire faster than their defined cadence, even if a player is
 * using an autoclicker that lands many hits per second.
 */
public final class ProcCooldownTracker {

    private static final Map<UUID, Map<String, Long>> LAST_PROC_MILLIS = new ConcurrentHashMap<>();

    private ProcCooldownTracker() {}

    /**
     * Returns true if the given player's proc of the given skill is ready to
     * fire (not on internal cooldown). If ready, marks it as fired now.
     *
     * @param player    The player attempting to proc the skill.
     * @param skillId   The stable skill id (e.g. "axes_devastating_blow").
     * @param cooldown  The internal cooldown in milliseconds. If &lt;= 0 the
     *                  proc is always ready and no state is recorded.
     * @return true if the proc may fire, false if it is still on cooldown.
     */
    public static boolean tryConsume(Player player, String skillId, long cooldown) {
        if (cooldown <= 0L) return true;
        long now = System.currentTimeMillis();
        Map<String, Long> skills = LAST_PROC_MILLIS.computeIfAbsent(
                player.getUniqueId(), k -> new ConcurrentHashMap<>());
        Long last = skills.get(skillId);
        if (last != null && now - last < cooldown) return false;
        skills.put(skillId, now);
        return true;
    }

    /**
     * Clears all tracked proc state for a player (e.g. on logout).
     */
    public static void clear(Player player) {
        LAST_PROC_MILLIS.remove(player.getUniqueId());
    }
}
