package com.magmaguy.elitemobs.advancedcombat.passives;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;

/** Owns short-lived passive facts without leaking them into persisted class progression. */
final class PassiveStateTracker {
    static final long RECENT_HIT_NANOS = 3_000_000_000L;
    static final long RECENT_ELITE_KILL_NANOS = 6_000_000_000L;
    private static final long NANOS_PER_TICK = 50_000_000L;

    private final LongSupplier nanoTime;
    private final Map<UUID, Long> recentlyHitUntil = new HashMap<>();
    private final Map<UUID, Long> recentEliteKillUntil = new HashMap<>();
    private final Map<UUID, Long> wardBrokenUntil = new HashMap<>();

    PassiveStateTracker() {
        this(System::nanoTime);
    }

    PassiveStateTracker(LongSupplier nanoTime) {
        this.nanoTime = nanoTime;
    }

    void recordHit(UUID playerId) {
        long now = nanoTime.getAsLong();
        recentlyHitUntil.put(playerId, saturatingAdd(now, RECENT_HIT_NANOS));
    }

    boolean recentlyHit(UUID playerId) {
        return active(recentlyHitUntil, playerId);
    }

    void recordEliteKill(UUID playerId) {
        recentEliteKillUntil.put(playerId,
                saturatingAdd(nanoTime.getAsLong(), RECENT_ELITE_KILL_NANOS));
    }

    boolean recentEliteKill(UUID playerId) {
        return active(recentEliteKillUntil, playerId);
    }

    void recordWardBroken(UUID playerId, int durationTicks) {
        if (durationTicks <= 0) return;
        wardBrokenUntil.put(playerId,
                saturatingAdd(nanoTime.getAsLong(), nanosForTicks(durationTicks)));
    }

    boolean wardBroken(UUID playerId) {
        return active(wardBrokenUntil, playerId);
    }

    void clear(UUID playerId) {
        recentlyHitUntil.remove(playerId);
        recentEliteKillUntil.remove(playerId);
        wardBrokenUntil.remove(playerId);
    }

    void clearAll() {
        recentlyHitUntil.clear();
        recentEliteKillUntil.clear();
        wardBrokenUntil.clear();
    }

    static long nanosForTicks(int ticks) {
        if (ticks <= 0) return 0L;
        return ticks > Long.MAX_VALUE / NANOS_PER_TICK
                ? Long.MAX_VALUE
                : ticks * NANOS_PER_TICK;
    }

    private boolean active(Map<UUID, Long> deadlines, UUID playerId) {
        Long deadline = deadlines.get(playerId);
        if (deadline == null) return false;
        if (deadline > nanoTime.getAsLong()) return true;
        deadlines.remove(playerId);
        return false;
    }

    private static long saturatingAdd(long left, long right) {
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }
}
