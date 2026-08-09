package com.magmaguy.elitemobs.combatsystem;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Owns one-shot overrides for programmatic damage calls.
 * <p>
 * Bukkit fires damage events synchronously. A caller opens a scope immediately around
 * {@code LivingEntity.damage(...)} and the matching EliteMobs filter consumes the override at the
 * beginning of that event. Consuming it before running downstream listeners prevents nested damage
 * events from inheriting the outer hit's override.
 */
public final class CombatDamageContext {

    private static final DamageOverride DEFAULT_OVERRIDE = new DamageOverride(false, 1.0);
    private static final ThreadLocal<Deque<PendingOverride>> PLAYER_TO_ELITE =
            ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Deque<PendingOverride>> ELITE_TO_PLAYER =
            ThreadLocal.withInitial(ArrayDeque::new);

    private CombatDamageContext() {
    }

    public static Scope bypassPlayerToElite() {
        return push(PLAYER_TO_ELITE, new DamageOverride(true, 1.0));
    }

    public static Scope bypassEliteToPlayer() {
        return push(ELITE_TO_PLAYER, new DamageOverride(true, 1.0));
    }

    public static Scope multiplyEliteToPlayer(double multiplier) {
        double safeMultiplier = Double.isFinite(multiplier) && multiplier >= 0 ? multiplier : 1.0;
        return push(ELITE_TO_PLAYER, new DamageOverride(false, safeMultiplier));
    }

    public static void runPlayerToEliteBypass(Runnable damageCall) {
        try (Scope ignored = bypassPlayerToElite()) {
            damageCall.run();
        }
    }

    public static void runEliteToPlayerBypass(Runnable damageCall) {
        try (Scope ignored = bypassEliteToPlayer()) {
            damageCall.run();
        }
    }

    public static void runEliteToPlayerMultiplier(double multiplier, Runnable damageCall) {
        try (Scope ignored = multiplyEliteToPlayer(multiplier)) {
            damageCall.run();
        }
    }

    public static DamageOverride consumePlayerToElite() {
        return consume(PLAYER_TO_ELITE);
    }

    public static DamageOverride consumeEliteToPlayer() {
        return consume(ELITE_TO_PLAYER);
    }

    private static Scope push(ThreadLocal<Deque<PendingOverride>> owner, DamageOverride override) {
        PendingOverride pendingOverride = new PendingOverride(override);
        Deque<PendingOverride> overrides = owner.get();
        overrides.addLast(pendingOverride);
        return () -> {
            Deque<PendingOverride> currentOverrides = owner.get();
            currentOverrides.removeLastOccurrence(pendingOverride);
            if (currentOverrides.isEmpty()) owner.remove();
        };
    }

    private static DamageOverride consume(ThreadLocal<Deque<PendingOverride>> owner) {
        Deque<PendingOverride> overrides = owner.get();
        PendingOverride pendingOverride = overrides.pollLast();
        if (overrides.isEmpty()) owner.remove();
        return pendingOverride == null ? DEFAULT_OVERRIDE : pendingOverride.override;
    }

    private static final class PendingOverride {
        private final DamageOverride override;

        private PendingOverride(DamageOverride override) {
            this.override = override;
        }
    }

    public record DamageOverride(boolean bypass, double specialMultiplier) {
    }

    @FunctionalInterface
    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }
}
