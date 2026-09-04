package com.magmaguy.elitemobs.combatsystem.combattag;

import java.util.UUID;

/**
 * Canonical per-player combat state for EliteMobs dungeon content.
 *
 * <p>The interface deliberately exposes state transitions rather than Bukkit events so healing,
 * presentation, and class resources can share one timeout without duplicating damage parsing.</p>
 */
public interface PlayerCombatState {

    boolean isInCombat(UUID playerId);

    void addListener(Listener listener);

    void removeListener(Listener listener);

    interface Listener {
        default void onCombatStarted(UUID playerId) {
        }

        default void onCombatEnded(UUID playerId) {
        }
    }
}
