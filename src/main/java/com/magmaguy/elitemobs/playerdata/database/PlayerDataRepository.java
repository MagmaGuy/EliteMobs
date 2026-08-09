package com.magmaguy.elitemobs.playerdata.database;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DatabaseConfig;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Serializes access to the player database and owns its connection and ordered write queue. */
final class PlayerDataRepository {

    private static final Object MONITOR = new Object();
    private static final Object SCORE_RANKING_MONITOR = new Object();
    private static final Deque<DatabaseUpdate> pendingUpdates = new ArrayDeque<>();
    private static final Map<UUID, Integer> cachedScores = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> scoreUpdatesBeforeCacheLoad = new HashMap<>();
    private static Connection connection;
    private static boolean drainScheduled;
    private static volatile boolean scoreRankingCacheLoaded;
    private static long scoreRankingLoadGeneration;

    private PlayerDataRepository() {
    }

    static Object monitor() {
        return MONITOR;
    }

    static Connection connection() throws Exception {
        synchronized (MONITOR) {
            File databaseFile = new File(MetadataHandler.PLUGIN.getDataFolder(), "data/" + PlayerData.getDATABASE_NAME());
            if (connection == null || connection.isClosed()) {
                if (!DatabaseConfig.isUseMySQL()) {
                    Class.forName("org.sqlite.JDBC");
                    connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
                } else {
                    Class.forName("com.mysql.jdbc.Driver");
                    String url = "jdbc:mysql://" + DatabaseConfig.getMysqlHost() + ":"
                            + DatabaseConfig.getMysqlPort() + "/" + DatabaseConfig.mysqlDatabaseName
                            + "?useSSL=" + DatabaseConfig.useSSL + "&createDatabaseIfNotExist=true";
                    connection = DriverManager.getConnection(
                            url, DatabaseConfig.getMysqlUsername(), DatabaseConfig.getMysqlPassword());
                }
                connection.setAutoCommit(true);
            }
            return connection;
        }
    }

    static boolean readPlayer(UUID playerId, ResultSetReader reader) throws Exception {
        synchronized (MONITOR) {
            String sql = "SELECT * FROM " + PlayerData.getPLAYER_DATA_TABLE_NAME() + " WHERE PlayerUUID = ?";
            try (PreparedStatement statement = connection().prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) return false;
                    reader.read(resultSet);
                    return true;
                }
            }
        }
    }

    static void insertNewPlayer(UUID playerId, String playerName) throws Exception {
        synchronized (MONITOR) {
            String sql = "INSERT INTO " + PlayerData.getPLAYER_DATA_TABLE_NAME() + " ("
                    + "PlayerUUID, DisplayName, CurrencyV2, CurrencyCents, Score, Kills, HighestLevelKilled,"
                    + " Deaths, QuestsCompleted, DungeonsCompleted, SkillXP_ARMOR, SkillXP_SWORDS, SkillXP_AXES, SkillXP_BOWS,"
                    + " SkillXP_CROSSBOWS, SkillXP_TRIDENTS, SkillXP_HOES, SkillXP_MACES, SkillXP_SPEARS,"
                    + " SkillBonusSelections, GamblingDebt, GamblingDebtCents, UseBookMenus, DismissEMStatusScreenMessage)"
                    + " VALUES (?, ?, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '{}', 0, 0, 1, 0)";
            try (PreparedStatement statement = connection().prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                statement.setString(2, playerName);
                statement.executeUpdate();
            }
            updateCachedScore(playerId, 0);
        }
    }

    static void enqueueUpdate(UUID playerId, String column, Object value) {
        validateColumn(column);
        synchronized (MONITOR) {
            pendingUpdates.addLast(new DatabaseUpdate(playerId, column, value));
            if (drainScheduled) return;
            drainScheduled = true;
        }
        if (!MetadataHandler.PLUGIN.isEnabled()) {
            drainUpdates();
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(MetadataHandler.PLUGIN, PlayerDataRepository::drainUpdates);
    }

    static void updateNow(UUID playerId, String column, Object value) {
        validateColumn(column);
        synchronized (MONITOR) {
            executeUpdate(new DatabaseUpdate(playerId, column, value));
        }
    }

    static Object getBlob(UUID playerId, String column) {
        return query(playerId, column, ResultSet::getBytes, null, "blob");
    }

    static Boolean getBoolean(UUID playerId, String column) {
        return query(playerId, column, ResultSet::getBoolean, null, "boolean");
    }

    static String getString(UUID playerId, String column) {
        return query(playerId, column, ResultSet::getString, null, "string");
    }

    static Integer getInteger(UUID playerId, String column) {
        return query(playerId, column, ResultSet::getInt, null, "integer");
    }

    static Long getLong(UUID playerId, String column) {
        return query(playerId, column, ResultSet::getLong, 0L, "long");
    }

    static PlayerData.ScoreRank getScoreRank(UUID playerId) {
        if (!scoreRankingCacheLoaded) return PlayerData.ScoreRank.unavailable();
        Integer playerScore = cachedScores.get(playerId);
        if (playerScore == null) return PlayerData.ScoreRank.unavailable();

        int position = 1;
        for (int score : cachedScores.values())
            if (score > playerScore) position++;
        return new PlayerData.ScoreRank(position, cachedScores.size());
    }

    static void updateCachedScore(UUID playerId, int score) {
        synchronized (SCORE_RANKING_MONITOR) {
            if (scoreRankingCacheLoaded) cachedScores.put(playerId, score);
            else scoreUpdatesBeforeCacheLoad.put(playerId, score);
        }
    }

    static void loadScoreRankingCacheAsync() {
        long loadGeneration;
        synchronized (SCORE_RANKING_MONITOR) {
            loadGeneration = ++scoreRankingLoadGeneration;
            scoreRankingCacheLoaded = false;
            cachedScores.clear();
        }
        Bukkit.getScheduler().runTaskAsynchronously(
                MetadataHandler.PLUGIN, () -> loadScoreRankingCache(loadGeneration));
    }

    private static void loadScoreRankingCache(long loadGeneration) {
        Map<UUID, Integer> loadedScores = new HashMap<>();
        synchronized (MONITOR) {
            String sql = "SELECT PlayerUUID, COALESCE(Score, 0) AS ScoreValue FROM "
                    + PlayerData.getPLAYER_DATA_TABLE_NAME();
            try (PreparedStatement statement = connection().prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    try {
                        loadedScores.put(UUID.fromString(resultSet.getString("PlayerUUID")),
                                resultSet.getInt("ScoreValue"));
                    } catch (IllegalArgumentException ignored) {
                        Logger.warn("Ignored invalid player UUID while loading the score ranking cache.");
                    }
                }
            } catch (Exception exception) {
                Logger.warn("Failed to load the player score ranking cache.");
                exception.printStackTrace();
                return;
            }
        }

        synchronized (SCORE_RANKING_MONITOR) {
            if (loadGeneration != scoreRankingLoadGeneration) return;
            loadedScores.putAll(scoreUpdatesBeforeCacheLoad);
            scoreUpdatesBeforeCacheLoad.clear();
            cachedScores.putAll(loadedScores);
            scoreRankingCacheLoaded = true;
        }
    }

    static void migrateCurrencyToCents() {
        synchronized (MONITOR) {
            try (Statement statement = connection().createStatement()) {
                int currencyRows = statement.executeUpdate("UPDATE " + PlayerData.getPLAYER_DATA_TABLE_NAME()
                        + " SET CurrencyCents = CAST(ROUND(CurrencyV2 * 100) AS INTEGER)"
                        + " WHERE (CurrencyCents IS NULL OR CurrencyCents = 0) AND CurrencyV2 > 0");
                int debtRows = statement.executeUpdate("UPDATE " + PlayerData.getPLAYER_DATA_TABLE_NAME()
                        + " SET GamblingDebtCents = CAST(ROUND(GamblingDebt * 100) AS INTEGER)"
                        + " WHERE (GamblingDebtCents IS NULL OR GamblingDebtCents = 0) AND GamblingDebt > 0");
                if (currencyRows > 0) Logger.info("Migrated " + currencyRows + " player currency rows to cent precision");
                if (debtRows > 0) Logger.info("Migrated " + debtRows + " player gambling debt rows to cent precision");
            } catch (Exception exception) {
                Logger.warn("Failed to migrate legacy currency/gambling debt columns to cents!");
                exception.printStackTrace();
            }
        }
    }

    static void importLegacy(Collection<LegacyPlayerData> legacyPlayers) throws Exception {
        synchronized (MONITOR) {
            Connection database = connection();
            boolean oldAutoCommit = database.getAutoCommit();
            database.setAutoCommit(false);
            try {
                String existsSql = "SELECT 1 FROM " + PlayerData.getPLAYER_DATA_TABLE_NAME() + " WHERE PlayerUUID = ?";
                String insertSql = "INSERT INTO " + PlayerData.getPLAYER_DATA_TABLE_NAME()
                        + " (PlayerUUID, DisplayName, CurrencyV2, CurrencyCents) VALUES (?, ?, ?, ?)";
                try (PreparedStatement exists = database.prepareStatement(existsSql);
                     PreparedStatement insert = database.prepareStatement(insertSql)) {
                    for (LegacyPlayerData legacy : legacyPlayers) {
                        exists.setString(1, legacy.playerId().toString());
                        try (ResultSet resultSet = exists.executeQuery()) {
                            if (resultSet.next()) continue;
                        }
                        insert.setString(1, legacy.playerId().toString());
                        insert.setString(2, legacy.displayName());
                        insert.setDouble(3, legacy.currency());
                        insert.setLong(4, Math.round(legacy.currency() * 100));
                        insert.executeUpdate();
                    }
                }
                database.commit();
            } catch (Exception exception) {
                database.rollback();
                throw exception;
            } finally {
                database.setAutoCommit(oldAutoCommit);
            }
        }
    }

    static void close() {
        synchronized (MONITOR) {
            drainUpdatesLocked();
            try {
                if (connection != null) connection.close();
            } catch (Exception exception) {
                Logger.warn("Could not correctly close database connection.");
            } finally {
                connection = null;
                drainScheduled = false;
                synchronized (SCORE_RANKING_MONITOR) {
                    scoreRankingLoadGeneration++;
                    cachedScores.clear();
                    scoreUpdatesBeforeCacheLoad.clear();
                    scoreRankingCacheLoaded = false;
                }
            }
        }
    }

    private static void drainUpdates() {
        synchronized (MONITOR) {
            drainUpdatesLocked();
            drainScheduled = false;
            if (!pendingUpdates.isEmpty()) {
                drainScheduled = true;
                Bukkit.getScheduler().runTaskAsynchronously(MetadataHandler.PLUGIN, PlayerDataRepository::drainUpdates);
            }
        }
    }

    private static void drainUpdatesLocked() {
        DatabaseUpdate update;
        while ((update = pendingUpdates.pollFirst()) != null) executeUpdate(update);
    }

    private static void executeUpdate(DatabaseUpdate update) {
        String sql = "UPDATE " + PlayerData.getPLAYER_DATA_TABLE_NAME() + " SET " + update.column() + " = ? WHERE PlayerUUID = ?";
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setObject(1, update.value());
            statement.setString(2, update.playerId().toString());
            statement.executeUpdate();
        } catch (Exception exception) {
            Logger.warn("Failed to update player database value " + update.column() + ".");
            exception.printStackTrace();
        }
    }

    private static <T> T query(UUID playerId, String column, ColumnReader<T> reader, T fallback, String type) {
        validateColumn(column);
        synchronized (MONITOR) {
            String sql = "SELECT " + column + " FROM " + PlayerData.getPLAYER_DATA_TABLE_NAME() + " WHERE PlayerUUID = ?";
            try (PreparedStatement statement = connection().prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) return fallback;
                    return reader.read(resultSet, column);
                }
            } catch (Exception exception) {
                Logger.warn("Failed to get " + type + " value from player database: " + column);
                exception.printStackTrace();
                return fallback;
            }
        }
    }

    private static void validateColumn(String column) {
        if (column == null || !column.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("Invalid player database column: " + column);
        }
    }

    record LegacyPlayerData(UUID playerId, String displayName, double currency) {
    }

    private record DatabaseUpdate(UUID playerId, String column, Object value) {
    }

    @FunctionalInterface
    interface ResultSetReader {
        void read(ResultSet resultSet) throws Exception;
    }

    @FunctionalInterface
    private interface ColumnReader<T> {
        T read(ResultSet resultSet, String column) throws Exception;
    }
}
