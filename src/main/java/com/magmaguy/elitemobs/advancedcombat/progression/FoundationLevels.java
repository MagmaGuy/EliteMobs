package com.magmaguy.elitemobs.advancedcombat.progression;

import java.util.Optional;
import java.util.UUID;

/**
 * Supplies a complete foundation-skill snapshot without coupling progression to PlayerData.
 * Implementations must be safe on both the caller thread and the asynchronous load thread,
 * and must read an existing cache rather than perform JDBC or other blocking I/O.
 */
@FunctionalInterface
public interface FoundationLevels {

    /** Returns empty until the player's complete PlayerData skill cache is published. */
    Optional<FoundationLevelSnapshot> snapshot(UUID playerId);
}
