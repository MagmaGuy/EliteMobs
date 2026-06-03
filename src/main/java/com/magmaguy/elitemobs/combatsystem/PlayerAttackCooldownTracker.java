package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.utils.GameClock;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks each player's last melee-hit tick so the combat
 * pipeline can derive melee attack charge from the real-time gap between
 * primary swings.
 *
 * <p>This is used by the player&rarr;elite damage formula instead of Bukkit's
 * {@code Player.getAttackCooldown()} value, which can be observed after the
 * swing has already consumed/reset the vanilla cooldown. The captured tick gap
 * still lets debug output distinguish properly paced swings from fast clicking.
 *
 * <h2>Design</h2>
 * <ul>
 *   <li><b>One {@link ConcurrentHashMap}</b> keyed by player UUID, valued by
 *       the {@link GameClock#getCurrentTick() game tick} of their last hit.
 *       O(1) read/write per hit.</li>
 *   <li><b>Lazy initialization.</b> The eviction sweep is only registered the
 *       first time {@link #recordHit(Player)} is called &mdash; the tracker
 *       imposes zero cost on servers where no one is fighting.</li>
 *   <li><b>Bounded memory.</b> A {@link GameClock}-scheduled sweep evicts
 *       UUIDs idle for more than {@link #IDLE_EVICTION_TICKS}, so a player
 *       who logs out and never returns does not leak.</li>
 *   <li><b>No quit-listener dependency.</b> The sweep handles cleanup,
 *       mirroring the existing pattern used by
 *       {@link com.magmaguy.elitemobs.combatsystem.antiexploit.AutoclickerThrottle}.</li>
 * </ul>
 */
public final class PlayerAttackCooldownTracker {

    /** Sweep interval: every 5 minutes (20 ticks/sec &times; 60 sec &times; 5 min = 6000 ticks). */
    public static final long SWEEP_INTERVAL_TICKS = 20L * 60L * 5L;

    /** Idle threshold before a UUID is evicted: 5 minutes of game ticks. */
    public static final long IDLE_EVICTION_TICKS = 20L * 60L * 5L;

    /** Sentinel returned when no previous hit is on file for the player. */
    public static final long NO_PREVIOUS_HIT = -1L;

    private static final Map<UUID, Long> LAST_HIT_TICK = new ConcurrentHashMap<>();
    private static volatile int sweepTaskId = -1;

    private PlayerAttackCooldownTracker() {
    }

    /**
     * Records a melee hit from the given player and returns the number of game
     * ticks elapsed since their previously tracked hit, or
     * {@link #NO_PREVIOUS_HIT} when this is their first recorded hit since
     * plugin start (or since their last eviction).
     *
     * <p>Lazily registers the eviction sweep on the first call.
     *
     * @param player attacking player; must be non-null
     * @return tick delta since previous hit, or {@link #NO_PREVIOUS_HIT}
     */
    public static long recordHit(Player player) {
        ensureSweepRegistered();
        long now = GameClock.getCurrentTick();
        Long previous = LAST_HIT_TICK.put(player.getUniqueId(), now);
        return previous == null ? NO_PREVIOUS_HIT : now - previous;
    }

    /**
     * Returns the game tick of the player's last recorded hit, or
     * {@link #NO_PREVIOUS_HIT} if nothing is tracked for that UUID.
     */
    public static long getLastHitTick(UUID uuid) {
        Long value = LAST_HIT_TICK.get(uuid);
        return value == null ? NO_PREVIOUS_HIT : value;
    }

    /** Forgets a player's last-hit tick. Safe to call for unknown UUIDs. */
    public static void forget(UUID uuid) {
        LAST_HIT_TICK.remove(uuid);
    }

    /** Drops all tracked entries. Used by {@link #shutdown()} and by tests. */
    public static void clearAll() {
        LAST_HIT_TICK.clear();
    }

    /**
     * Cancels the eviction sweep and clears tracked state. Call from the
     * EliteMobs plugin disable hook before {@link GameClock#shutdown()}.
     */
    public static void shutdown() {
        int taskId = sweepTaskId;
        if (taskId != -1) {
            GameClock.cancel(taskId);
            sweepTaskId = -1;
        }
        clearAll();
    }

    /**
     * Lazily registers the eviction sweep on the {@link GameClock}.
     * <p>Double-checked locking on {@link #sweepTaskId} (declared volatile)
     * keeps the fast path lock-free after the first hit.
     */
    private static void ensureSweepRegistered() {
        if (sweepTaskId != -1) return;
        synchronized (PlayerAttackCooldownTracker.class) {
            if (sweepTaskId != -1) return;
            sweepTaskId = GameClock.scheduleRepeating(
                    SWEEP_INTERVAL_TICKS, SWEEP_INTERVAL_TICKS,
                    PlayerAttackCooldownTracker::sweep);
        }
    }

    /** Evicts entries whose last-hit tick is older than {@link #IDLE_EVICTION_TICKS}. */
    private static void sweep() {
        long now = GameClock.getCurrentTick();
        LAST_HIT_TICK.entrySet().removeIf(entry -> now - entry.getValue() > IDLE_EVICTION_TICKS);
    }
}
