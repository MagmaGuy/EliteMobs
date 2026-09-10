package com.magmaguy.elitemobs.experimentalcombat.progression;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Synchronous persistence boundary for [Alpha] Advanced Combat System progression.
 *
 * <p>Implementations must be safe to call from asynchronous player-data tasks. This interface
 * does not schedule work; callers choose the appropriate execution context.</p>
 */
public interface ClassProgressionStore {

    StoredClassProfile loadOrCreateProfile(UUID playerId, int catalogVersion) throws SQLException;

    List<StoredClassProgress> loadAllProgress(UUID playerId) throws SQLException;

    /** Returns an in-memory zero record when the form has no persisted row. */
    StoredClassProgress loadProgressOrZero(UUID playerId, String formId, int catalogVersion) throws SQLException;

    void saveProfile(StoredClassProfile profile) throws SQLException;

    void saveProgress(StoredClassProgress progress) throws SQLException;

    /** Atomically saves one player's profile and the supplied form rows. */
    void savePlayerAggregate(
            StoredClassProfile profile,
            Collection<StoredClassProgress> progress) throws SQLException;
}
