package com.magmaguy.elitemobs.playerdata.database;

import com.magmaguy.elitemobs.config.DatabaseConfig;
import com.magmaguy.elitemobs.experimentalcombat.progression.ClassProgressionStore;
import com.magmaguy.elitemobs.experimentalcombat.progression.StoredClassProfile;
import com.magmaguy.elitemobs.experimentalcombat.progression.StoredClassProgress;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** JDBC adapter backed by the player-data repository's serialized connection. */
public final class JdbcClassProgressionStore implements ClassProgressionStore {

    static final String PROFILE_TABLE = "PlayerClassProfile";
    static final String PROGRESS_TABLE = "PlayerClassProgress";

    @Override
    public StoredClassProfile loadOrCreateProfile(UUID playerId, int catalogVersion) throws SQLException {
        Objects.requireNonNull(playerId, "playerId");
        if (catalogVersion < 0) throw new IllegalArgumentException("catalogVersion must not be negative");

        synchronized (PlayerDataRepository.jdbcMonitor()) {
            Connection connection = connection();
            try (PreparedStatement statement = connection.prepareStatement(insertProfileIfAbsentSql())) {
                statement.setString(1, playerId.toString());
                statement.setInt(2, catalogVersion);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT SelectedFormId, SelectedInputId, CatalogVersion, TutorialSkillsUsed FROM "
                            + PROFILE_TABLE + " WHERE PlayerUUID = ?")) {
                statement.setString(1, playerId.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next())
                        throw new SQLException("Failed to create Experimental Combat profile for " + playerId);
                    return readProfile(playerId, resultSet);
                }
            }
        }
    }

    @Override
    public List<StoredClassProgress> loadAllProgress(UUID playerId) throws SQLException {
        Objects.requireNonNull(playerId, "playerId");
        synchronized (PlayerDataRepository.jdbcMonitor()) {
            String sql = "SELECT FormId, XP, CatalogVersion, ChallengeCompleted FROM " + PROGRESS_TABLE
                    + " WHERE PlayerUUID = ? ORDER BY FormId";
            try (PreparedStatement statement = connection().prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<StoredClassProgress> progress = new ArrayList<>();
                    while (resultSet.next()) progress.add(readProgress(playerId, resultSet));
                    return List.copyOf(progress);
                }
            }
        }
    }

    @Override
    public StoredClassProgress loadProgressOrZero(UUID playerId, String formId, int catalogVersion) throws SQLException {
        StoredClassProgress zero = StoredClassProgress.zero(playerId, formId, catalogVersion);
        synchronized (PlayerDataRepository.jdbcMonitor()) {
            String sql = "SELECT FormId, XP, CatalogVersion, ChallengeCompleted FROM " + PROGRESS_TABLE
                    + " WHERE PlayerUUID = ? AND FormId = ?";
            try (PreparedStatement statement = connection().prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                statement.setString(2, formId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? readProgress(playerId, resultSet) : zero;
                }
            }
        }
    }

    @Override
    public void saveProfile(StoredClassProfile profile) throws SQLException {
        Objects.requireNonNull(profile, "profile");
        synchronized (PlayerDataRepository.jdbcMonitor()) {
            try (PreparedStatement statement = connection().prepareStatement(upsertProfileSql())) {
                bindProfile(statement, profile);
                statement.executeUpdate();
            }
        }
    }

    @Override
    public void saveProgress(StoredClassProgress progress) throws SQLException {
        Objects.requireNonNull(progress, "progress");
        synchronized (PlayerDataRepository.jdbcMonitor()) {
            try (PreparedStatement statement = connection().prepareStatement(upsertProgressSql())) {
                statement.setString(1, progress.playerId().toString());
                statement.setString(2, progress.formId());
                statement.setLong(3, progress.xp());
                statement.setInt(4, progress.catalogVersion());
                statement.setBoolean(5, progress.challengeCompleted());
                statement.executeUpdate();
            }
        }
    }

    @Override
    public void savePlayerAggregate(
            StoredClassProfile profile,
            Collection<StoredClassProgress> progressRows) throws SQLException {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(progressRows, "progressRows");
        for (StoredClassProgress progress : progressRows) {
            Objects.requireNonNull(progress, "progressRows contains null");
            if (!profile.playerId().equals(progress.playerId()))
                throw new IllegalArgumentException("Aggregate contains progress for another player");
            if (profile.catalogVersion() != progress.catalogVersion())
                throw new IllegalArgumentException("Aggregate contains mixed catalog versions");
        }

        synchronized (PlayerDataRepository.jdbcMonitor()) {
            Connection connection = connection();
            if (!connection.getAutoCommit())
                throw new SQLException("Class progression aggregate requires an idle auto-commit connection");
            connection.setAutoCommit(false);
            Throwable failure = null;
            try {
                try (PreparedStatement statement = connection.prepareStatement(upsertProfileSql())) {
                    bindProfile(statement, profile);
                    statement.executeUpdate();
                }
                if (!progressRows.isEmpty()) {
                    try (PreparedStatement statement = connection.prepareStatement(upsertProgressSql())) {
                        for (StoredClassProgress progress : progressRows) {
                            bindProgress(statement, progress);
                            statement.addBatch();
                        }
                        statement.executeBatch();
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                failure = exception;
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    exception.addSuppressed(rollbackFailure);
                }
                throw exception;
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException restoreFailure) {
                    if (failure != null) failure.addSuppressed(restoreFailure);
                    else throw restoreFailure;
                }
            }
        }
    }

    private static StoredClassProfile readProfile(UUID playerId, ResultSet resultSet) throws SQLException {
        return new StoredClassProfile(
                playerId,
                resultSet.getString("SelectedFormId"),
                resultSet.getString("SelectedInputId"),
                resultSet.getInt("CatalogVersion"), resultSet.getInt("TutorialSkillsUsed"));
    }

    private static StoredClassProgress readProgress(UUID playerId, ResultSet resultSet) throws SQLException {
        return new StoredClassProgress(
                playerId,
                resultSet.getString("FormId"),
                resultSet.getLong("XP"),
                resultSet.getInt("CatalogVersion"), resultSet.getBoolean("ChallengeCompleted"));
    }

    private static void setNullableString(PreparedStatement statement, int parameter, String value)
            throws SQLException {
        if (value == null) statement.setNull(parameter, Types.VARCHAR);
        else statement.setString(parameter, value);
    }

    private static void bindProfile(PreparedStatement statement, StoredClassProfile profile)
            throws SQLException {
        statement.setString(1, profile.playerId().toString());
        setNullableString(statement, 2, profile.selectedFormId());
        setNullableString(statement, 3, profile.selectedInputId());
        statement.setInt(4, profile.catalogVersion());
        statement.setInt(5, profile.tutorialSkillsUsed());
    }

    private static void bindProgress(PreparedStatement statement, StoredClassProgress progress)
            throws SQLException {
        statement.setString(1, progress.playerId().toString());
        statement.setString(2, progress.formId());
        statement.setLong(3, progress.xp());
        statement.setInt(4, progress.catalogVersion());
        statement.setBoolean(5, progress.challengeCompleted());
    }

    private static String insertProfileIfAbsentSql() {
        String insert = "INSERT INTO " + PROFILE_TABLE + " (PlayerUUID, CatalogVersion) VALUES (?, ?)";
        if (DatabaseConfig.isUseMySQL())
            return insert + " ON DUPLICATE KEY UPDATE PlayerUUID = VALUES(PlayerUUID)";
        return insert + " ON CONFLICT(PlayerUUID) DO NOTHING";
    }

    private static String upsertProfileSql() {
        String insert = "INSERT INTO " + PROFILE_TABLE
                + " (PlayerUUID, SelectedFormId, SelectedInputId, CatalogVersion, TutorialSkillsUsed) VALUES (?, ?, ?, ?, ?)";
        if (DatabaseConfig.isUseMySQL()) {
            return insert + " ON DUPLICATE KEY UPDATE"
                    + " SelectedFormId = VALUES(SelectedFormId),"
                    + " SelectedInputId = VALUES(SelectedInputId),"
                    + " CatalogVersion = VALUES(CatalogVersion), TutorialSkillsUsed = VALUES(TutorialSkillsUsed)";
        }
        return insert + " ON CONFLICT(PlayerUUID) DO UPDATE SET"
                + " SelectedFormId = excluded.SelectedFormId,"
                + " SelectedInputId = excluded.SelectedInputId,"
                + " CatalogVersion = excluded.CatalogVersion, TutorialSkillsUsed = excluded.TutorialSkillsUsed";
    }

    private static String upsertProgressSql() {
        String insert = "INSERT INTO " + PROGRESS_TABLE
                + " (PlayerUUID, FormId, XP, CatalogVersion, ChallengeCompleted) VALUES (?, ?, ?, ?, ?)";
        if (DatabaseConfig.isUseMySQL()) {
            return insert + " ON DUPLICATE KEY UPDATE"
                    + " XP = VALUES(XP),"
                    + " CatalogVersion = VALUES(CatalogVersion), ChallengeCompleted = VALUES(ChallengeCompleted)";
        }
        return insert + " ON CONFLICT(PlayerUUID, FormId) DO UPDATE SET"
                + " XP = excluded.XP,"
                + " CatalogVersion = excluded.CatalogVersion, ChallengeCompleted = excluded.ChallengeCompleted";
    }

    private static Connection connection() throws SQLException {
        try {
            return PlayerDataRepository.connection();
        } catch (SQLException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new SQLException("Could not open the player-data connection", exception);
        }
    }
}
