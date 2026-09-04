package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;

/**
 * Read-only bridge from passive evaluation to the authoritative active-control runtimes.
 * Ordinary mob aggro is deliberately absent from this contract.
 */
public final class ClassControlAttribution {
    private static final BiPredicate<Player, LivingEntity> NONE = (player, target) -> false;
    private static final AtomicReference<BiPredicate<Player, LivingEntity>> QUERY =
            new AtomicReference<>(NONE);

    private ClassControlAttribution() {
    }

    public static AutoCloseable install(BiPredicate<Player, LivingEntity> query) {
        Objects.requireNonNull(query, "query");
        QUERY.set(query);
        return () -> QUERY.compareAndSet(query, NONE);
    }

    public static boolean controlledBy(Player player, LivingEntity target) {
        if (player == null || target == null) return false;
        try {
            return QUERY.get().test(player, target);
        } catch (RuntimeException staleRuntime) {
            return false;
        }
    }
}
