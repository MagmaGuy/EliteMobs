package com.magmaguy.elitemobs.playerdata.database;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DatabaseConfig;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Stores dungeon state that changes during gameplay without rewriting content YAML files. */
public final class DungeonRuntimeData {

    private static final Logger LOGGER = Logger.getLogger("EliteMobs");

    static final String REGIONAL_BOSS_TABLE = "RegionalBossCooldowns";
    static final String TREASURE_CHEST_TABLE = "TreasureChestCooldowns";
    static final String TREASURE_CHEST_PLAYER_TABLE = "TreasureChestPlayerCooldowns";
    private static final Object QUEUE_MONITOR = new Object();
    private static final Map<String, RuntimeWrite> pendingWrites = new LinkedHashMap<>();
    private static final Map<String, Long> regionalBossCooldowns = new HashMap<>();
    private static final Map<String, Long> treasureChestCooldowns = new HashMap<>();
    private static final Map<String, Map<UUID, Long>> treasureChestPlayerCooldowns = new HashMap<>();
    private static boolean drainScheduled;
    private static boolean draining;
    private static int consecutiveWriteFailures;
    private static volatile boolean available;

    private DungeonRuntimeData() {
    }

    static void initializeSchema(Connection connection) throws SQLException {
        available = false;
        if (runtimeNamespace().length() > 64)
            throw new SQLException("mysqlServerId may not exceed 64 characters");
        ensureRuntimeTable(connection, REGIONAL_BOSS_TABLE, "LocationKey CHAR(64) NOT NULL", "RespawnAt");
        ensureRuntimeTable(connection, TREASURE_CHEST_TABLE, "LocationKey CHAR(64) NOT NULL", "RestockAt");
        ensureRuntimeTable(connection, TREASURE_CHEST_PLAYER_TABLE, "PlayerUUID VARCHAR(36) NOT NULL", "RestockAt");
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + TREASURE_CHEST_PLAYER_TABLE
                + " WHERE ServerId = ? AND RestockAt <= ?")) {
            statement.setString(1, runtimeNamespace());
            statement.setLong(2, System.currentTimeMillis() / 1000L);
            statement.executeUpdate();
        }
        synchronized (QUEUE_MONITOR) {
            regionalBossCooldowns.clear();
            treasureChestCooldowns.clear();
            treasureChestPlayerCooldowns.clear();
            loadCooldownTable(connection, REGIONAL_BOSS_TABLE, "RespawnAt", regionalBossCooldowns);
            loadCooldownTable(connection, TREASURE_CHEST_TABLE, "RestockAt", treasureChestCooldowns);
            try (PreparedStatement statement = connection.prepareStatement("SELECT ConfigFile, PlayerUUID, RestockAt FROM "
                    + TREASURE_CHEST_PLAYER_TABLE + " WHERE ServerId = ?")) {
                statement.setString(1, runtimeNamespace());
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        try {
                            treasureChestPlayerCooldowns
                                    .computeIfAbsent(resultSet.getString("ConfigFile"), ignored -> new HashMap<>())
                                    .put(UUID.fromString(resultSet.getString("PlayerUUID")), resultSet.getLong("RestockAt"));
                        } catch (IllegalArgumentException ignored) {
                            LOGGER.warning("Ignored invalid player UUID in " + TREASURE_CHEST_PLAYER_TABLE + ".");
                        }
                    }
                }
            }
        }
        available = true;
    }

    private static void ensureRuntimeTable(Connection connection, String table, String identityColumn,
                                           String valueColumn) throws SQLException {
        if (!tableExists(connection, table)) {
            createRuntimeTable(connection, table, identityColumn, valueColumn);
            return;
        }
        if (columnExists(connection, table, "ServerId")) return;

        String identityName = identityColumn.substring(0, identityColumn.indexOf(' '));
        String product = connection.getMetaData().getDatabaseProductName().toLowerCase();
        if (product.contains("mysql") || product.contains("mariadb")) {
            String namespace = runtimeNamespace().replace("'", "''");
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE " + table
                        + " ADD COLUMN ServerId VARCHAR(64) NOT NULL DEFAULT '" + namespace + "' FIRST,"
                        + " DROP PRIMARY KEY, ADD PRIMARY KEY (ServerId, ConfigFile, " + identityName + ")");
            } catch (SQLException exception) {
                if (!columnExists(connection, table, "ServerId")) throw exception;
            }
            return;
        }

        String legacyTable = table + "_LegacyRuntimeMigration";
        boolean oldAutoCommit = connection.getAutoCommit();
        if (!oldAutoCommit) throw new SQLException("Runtime schema migration requires its own transaction");
        connection.setAutoCommit(false);
        try {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE " + table + " RENAME TO " + legacyTable);
            }
            createRuntimeTable(connection, table, identityColumn, valueColumn);
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + table
                    + " (ServerId, ConfigFile, " + identityName + ", " + valueColumn + ")"
                    + " SELECT ?, ConfigFile, " + identityName + ", " + valueColumn + " FROM " + legacyTable)) {
                statement.setString(1, runtimeNamespace());
                statement.executeUpdate();
            }
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DROP TABLE " + legacyTable);
            }
            connection.commit();
        } catch (SQLException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(oldAutoCommit);
        }
    }

    private static void createRuntimeTable(Connection connection, String table, String identityColumn,
                                           String valueColumn) throws SQLException {
        String identityName = identityColumn.substring(0, identityColumn.indexOf(' '));
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + table + " (ServerId VARCHAR(64) NOT NULL, "
                    + "ConfigFile VARCHAR(255) NOT NULL, " + identityColumn + ", " + valueColumn + " BIGINT NOT NULL, "
                    + "PRIMARY KEY (ServerId, ConfigFile, " + identityName + "))");
        }
    }

    private static boolean tableExists(Connection connection, String table) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), null, table, new String[]{"TABLE"})) {
            return resultSet.next();
        }
    }

    private static boolean columnExists(Connection connection, String table, String column) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), null, table, column)) {
            return resultSet.next();
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    public static long loadRegionalBossCooldown(String configFile, String location, long legacyValue) {
        return loadCooldown(REGIONAL_BOSS_TABLE, "RespawnAt", configFile, location, legacyValue);
    }

    public static long loadTreasureChestCooldown(String configFile, String location, long legacyValue) {
        return loadCooldown(TREASURE_CHEST_TABLE, "RestockAt", configFile, location, legacyValue);
    }

    public static List<String> loadTreasureChestPlayerCooldowns(String configFile, List<String> legacyValues) {
        List<String> safeLegacyValues = legacyValues == null ? new ArrayList<>() : legacyValues;
        if (!available) return safeLegacyValues;
        List<String> stored = cachedPlayerCooldowns(configFile);
        if (!stored.isEmpty() || safeLegacyValues.isEmpty()) return stored;
        synchronized (PlayerDataRepository.jdbcMonitor()) {
            try {
                Connection connection = PlayerDataRepository.connection();
                boolean oldAutoCommit = connection.getAutoCommit();
                try {
                    connection.setAutoCommit(false);
                    for (String legacyValue : safeLegacyValues) {
                        String[] split = legacyValue.split(":", 2);
                        if (split.length != 2) continue;
                        try {
                            upsertPlayerCooldown(connection, configFile, UUID.fromString(split[0]), Long.parseLong(split[1]));
                        } catch (IllegalArgumentException ignored) {
                            LOGGER.warning("Ignored invalid legacy treasure chest cooldown in " + configFile + ": " + legacyValue);
                        }
                    }
                    connection.commit();
                    synchronized (QUEUE_MONITOR) {
                        Map<UUID, Long> cooldowns = treasureChestPlayerCooldowns
                                .computeIfAbsent(configFile, ignored -> new HashMap<>());
                        for (String legacyValue : safeLegacyValues) {
                            String[] split = legacyValue.split(":", 2);
                            if (split.length != 2) continue;
                            try {
                                cooldowns.put(UUID.fromString(split[0]), Long.parseLong(split[1]));
                            } catch (IllegalArgumentException ignored) {
                                // Invalid values were already reported while importing.
                            }
                        }
                    }
                } catch (Exception exception) {
                    connection.rollback();
                    throw exception;
                } finally {
                    connection.setAutoCommit(oldAutoCommit);
                }
                return cachedPlayerCooldowns(configFile);
            } catch (Exception exception) {
                available = false;
                LOGGER.warning("Failed to load treasure chest player cooldowns for " + configFile + ".");
                exception.printStackTrace();
                return safeLegacyValues;
            }
        }
    }

    public static boolean saveRegionalBossCooldown(String configFile, String location, long respawnAt) {
        if (!available) return false;
        synchronized (QUEUE_MONITOR) {
            regionalBossCooldowns.put(cooldownKey(configFile, location), respawnAt);
        }
        return enqueue("boss:" + configFile + ":" + locationKey(location), "regional boss cooldown for " + configFile,
                connection -> upsertCooldown(connection, REGIONAL_BOSS_TABLE, "RespawnAt", configFile, location, respawnAt));
    }

    public static boolean clearRegionalBossCooldown(String configFile, String location) {
        if (!available) return false;
        synchronized (QUEUE_MONITOR) {
            regionalBossCooldowns.remove(cooldownKey(configFile, location));
        }
        return enqueue("boss:" + configFile + ":" + locationKey(location), "regional boss cooldown removal for " + configFile,
                connection -> deleteCooldown(connection, REGIONAL_BOSS_TABLE, configFile, location));
    }

    public static boolean saveTreasureChestCooldown(String configFile, String location, long restockAt) {
        if (!available) return false;
        synchronized (QUEUE_MONITOR) {
            treasureChestCooldowns.put(cooldownKey(configFile, location), restockAt);
        }
        return enqueue("chest:" + configFile + ":" + locationKey(location), "treasure chest cooldown for " + configFile,
                connection -> upsertCooldown(connection, TREASURE_CHEST_TABLE, "RestockAt", configFile, location, restockAt));
    }

    public static boolean clearTreasureChestCooldown(String configFile, String location) {
        if (!available) return false;
        synchronized (QUEUE_MONITOR) {
            treasureChestCooldowns.remove(cooldownKey(configFile, location));
        }
        return enqueue("chest:" + configFile + ":" + locationKey(location), "treasure chest cooldown removal for " + configFile,
                connection -> deleteCooldown(connection, TREASURE_CHEST_TABLE, configFile, location));
    }

    public static boolean saveTreasureChestPlayerCooldown(String configFile, UUID playerId, long restockAt) {
        if (!available) return false;
        synchronized (QUEUE_MONITOR) {
            treasureChestPlayerCooldowns.computeIfAbsent(configFile, ignored -> new HashMap<>()).put(playerId, restockAt);
        }
        return enqueue("chest-player:" + configFile + ":" + playerId, "treasure chest player cooldown for " + configFile,
                connection -> upsertPlayerCooldown(connection, configFile, playerId, restockAt));
    }

    public static boolean clearTreasureChestPlayerCooldown(String configFile, UUID playerId) {
        if (!available) return false;
        synchronized (QUEUE_MONITOR) {
            Map<UUID, Long> cooldowns = treasureChestPlayerCooldowns.get(configFile);
            if (cooldowns != null) cooldowns.remove(playerId);
        }
        return enqueue("chest-player:" + configFile + ":" + playerId, "treasure chest player cooldown removal for " + configFile, connection -> {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + TREASURE_CHEST_PLAYER_TABLE
                    + " WHERE ServerId = ? AND ConfigFile = ? AND PlayerUUID = ?")) {
                statement.setString(1, runtimeNamespace());
                statement.setString(2, configFile);
                statement.setString(3, playerId.toString());
                statement.executeUpdate();
            }
        });
    }

    public static boolean clearTreasureChestCooldowns(String configFile) {
        if (!available) return false;
        synchronized (QUEUE_MONITOR) {
            pendingWrites.keySet().removeIf(key -> key.startsWith("chest:" + configFile + ":")
                    || key.startsWith("chest-player:" + configFile + ":"));
            treasureChestCooldowns.keySet().removeIf(key -> key.startsWith(configFile + "\0"));
            treasureChestPlayerCooldowns.remove(configFile);
        }
        return enqueue("chest-clear:" + configFile, "treasure chest cooldown removal for " + configFile, connection -> {
            for (String table : List.of(TREASURE_CHEST_TABLE, TREASURE_CHEST_PLAYER_TABLE))
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table
                        + " WHERE ServerId = ? AND ConfigFile = ?")) {
                    statement.setString(1, runtimeNamespace());
                    statement.setString(2, configFile);
                    statement.executeUpdate();
                }
        });
    }

    public static void shutdown() {
        available = false;
        while (true) {
            synchronized (QUEUE_MONITOR) {
                while (draining)
                    try {
                        QUEUE_MONITOR.wait();
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        LOGGER.warning("Interrupted while flushing dungeon runtime state.");
                        return;
                    }
                if (pendingWrites.isEmpty()) return;
            }
            if (!drainWrites()) {
                LOGGER.warning("Dungeon runtime shutdown flush failed; pending changes could not be persisted.");
                return;
            }
        }
    }

    private static long loadCooldown(String table, String valueColumn, String configFile, String location, long legacyValue) {
        if (!available) return legacyValue;
        Map<String, Long> cache = table.equals(REGIONAL_BOSS_TABLE)
                ? regionalBossCooldowns
                : treasureChestCooldowns;
        synchronized (QUEUE_MONITOR) {
            Long stored = cache.get(cooldownKey(configFile, location));
            if (stored != null) return stored;
        }
        if (legacyValue <= 0) return legacyValue;
        synchronized (PlayerDataRepository.jdbcMonitor()) {
            try {
                Connection connection = PlayerDataRepository.connection();
                upsertCooldown(connection, table, valueColumn, configFile, location, legacyValue);
                synchronized (QUEUE_MONITOR) {
                    cache.put(cooldownKey(configFile, location), legacyValue);
                }
                return legacyValue;
            } catch (Exception exception) {
                available = false;
                LOGGER.warning("Failed to load dungeon runtime cooldown for " + configFile + ".");
                exception.printStackTrace();
                return legacyValue;
            }
        }
    }

    private static boolean enqueue(String key, String description, SqlOperation operation) {
        if (!available) return false;
        synchronized (QUEUE_MONITOR) {
            if (!available) return false;
            pendingWrites.put(key, new RuntimeWrite(description, operation));
            if (drainScheduled) return true;
            drainScheduled = true;
        }
        if (!MetadataHandler.PLUGIN.isEnabled()) {
            drainWrites();
            return true;
        }
        Bukkit.getScheduler().runTaskLaterAsynchronously(MetadataHandler.PLUGIN, DungeonRuntimeData::drainWrites, 20L);
        return true;
    }

    static boolean drainWrites() {
        Map<String, RuntimeWrite> writes;
        boolean committed = false;
        synchronized (QUEUE_MONITOR) {
            if (draining) return true;
            writes = new LinkedHashMap<>(pendingWrites);
            if (writes.isEmpty()) {
                drainScheduled = false;
                return true;
            }
            draining = true;
        }
        try {
            synchronized (PlayerDataRepository.jdbcMonitor()) {
                Connection connection = null;
                boolean oldAutoCommit = true;
                try {
                    connection = PlayerDataRepository.connection();
                    oldAutoCommit = connection.getAutoCommit();
                    connection.setAutoCommit(false);
                    for (RuntimeWrite write : writes.values())
                        write.operation().execute(connection);
                    connection.commit();
                    committed = true;
                } catch (Exception exception) {
                    if (connection != null)
                        try {
                            connection.rollback();
                        } catch (SQLException rollbackException) {
                            exception.addSuppressed(rollbackException);
                        }
                    consecutiveWriteFailures++;
                    if (consecutiveWriteFailures == 1 || consecutiveWriteFailures % 10 == 0) {
                        LOGGER.warning("Failed to save " + writes.size() + " dungeon runtime changes (attempt "
                                + consecutiveWriteFailures + "); retained for retry. First entry: "
                                + writes.values().iterator().next().description() + ".");
                        exception.printStackTrace();
                    }
                } finally {
                    if (connection != null)
                        try {
                            connection.setAutoCommit(oldAutoCommit);
                        } catch (SQLException exception) {
                            LOGGER.warning("Failed to restore database auto-commit after saving dungeon runtime state.");
                            exception.printStackTrace();
                        }
                }
            }
        } finally {
            boolean scheduleNext;
            synchronized (QUEUE_MONITOR) {
                if (committed) {
                    // A newer value may have arrived while JDBC was busy. Only acknowledge the
                    // exact writes in this transaction; failed batches stay queued in order.
                    writes.forEach((key, write) -> pendingWrites.remove(key, write));
                    if (consecutiveWriteFailures > 0)
                        LOGGER.info("Dungeon runtime persistence recovered after " + consecutiveWriteFailures + " failed attempts.");
                    consecutiveWriteFailures = 0;
                }
                draining = false;
                scheduleNext = available && !pendingWrites.isEmpty();
                if (!scheduleNext) drainScheduled = false;
                QUEUE_MONITOR.notifyAll();
            }
            if (scheduleNext)
                Bukkit.getScheduler().runTaskLaterAsynchronously(MetadataHandler.PLUGIN, DungeonRuntimeData::drainWrites,
                        Math.min(1200L, 20L << Math.min(consecutiveWriteFailures, 6)));
        }
        return committed;
    }

    static Long readCooldown(Connection connection, String table, String valueColumn,
                             String configFile, String location) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT " + valueColumn + " FROM " + table
                + " WHERE ServerId = ? AND ConfigFile = ? AND LocationKey = ?")) {
            statement.setString(1, runtimeNamespace());
            statement.setString(2, configFile);
            statement.setString(3, locationKey(location));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getLong(valueColumn) : null;
            }
        }
    }

    static void upsertCooldown(Connection connection, String table, String valueColumn,
                               String configFile, String location, long value) throws SQLException {
        String sql = "INSERT INTO " + table + " (ServerId, ConfigFile, LocationKey, " + valueColumn + ") VALUES (?, ?, ?, ?)"
                + upsertClause(connection, "LocationKey", valueColumn);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, runtimeNamespace());
            statement.setString(2, configFile);
            statement.setString(3, locationKey(location));
            statement.setLong(4, value);
            statement.setLong(5, value);
            statement.executeUpdate();
        }
    }

    static void deleteCooldown(Connection connection, String table, String configFile, String location) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table
                + " WHERE ServerId = ? AND ConfigFile = ? AND LocationKey = ?")) {
            statement.setString(1, runtimeNamespace());
            statement.setString(2, configFile);
            statement.setString(3, locationKey(location));
            statement.executeUpdate();
        }
    }

    static List<String> readPlayerCooldowns(Connection connection, String configFile) throws SQLException {
        List<String> cooldowns = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT PlayerUUID, RestockAt FROM "
                + TREASURE_CHEST_PLAYER_TABLE + " WHERE ServerId = ? AND ConfigFile = ?")) {
            statement.setString(1, runtimeNamespace());
            statement.setString(2, configFile);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next())
                    cooldowns.add(resultSet.getString("PlayerUUID") + ":" + resultSet.getLong("RestockAt"));
            }
        }
        return cooldowns;
    }

    private static List<String> cachedPlayerCooldowns(String configFile) {
        synchronized (QUEUE_MONITOR) {
            Map<UUID, Long> cooldowns = treasureChestPlayerCooldowns.get(configFile);
            if (cooldowns == null) return new ArrayList<>();
            List<String> serialized = new ArrayList<>(cooldowns.size());
            cooldowns.forEach((playerId, restockAt) -> serialized.add(playerId + ":" + restockAt));
            return serialized;
        }
    }

    private static void loadCooldownTable(Connection connection, String table, String valueColumn,
                                          Map<String, Long> destination) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT ConfigFile, LocationKey, " + valueColumn
                + " FROM " + table + " WHERE ServerId = ?")) {
            statement.setString(1, runtimeNamespace());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next())
                    destination.put(resultSet.getString("ConfigFile") + "\0" + resultSet.getString("LocationKey"),
                            resultSet.getLong(valueColumn));
            }
        }
    }

    static void upsertPlayerCooldown(Connection connection, String configFile, UUID playerId, long restockAt) throws SQLException {
        String sql = "INSERT INTO " + TREASURE_CHEST_PLAYER_TABLE
                + " (ServerId, ConfigFile, PlayerUUID, RestockAt) VALUES (?, ?, ?, ?)"
                + upsertClause(connection, "PlayerUUID", "RestockAt");
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, runtimeNamespace());
            statement.setString(2, configFile);
            statement.setString(3, playerId.toString());
            statement.setLong(4, restockAt);
            statement.setLong(5, restockAt);
            statement.executeUpdate();
        }
    }

    private static String upsertClause(Connection connection, String identity, String value) throws SQLException {
        String product = connection.getMetaData().getDatabaseProductName().toLowerCase(java.util.Locale.ROOT);
        return (product.contains("mysql") || product.contains("mariadb"))
                ? " ON DUPLICATE KEY UPDATE " + value + " = ?"
                : " ON CONFLICT (ServerId, ConfigFile, " + identity + ") DO UPDATE SET " + value + " = ?";
    }

    private static String locationKey(String location) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(location.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static String runtimeNamespace() {
        String configured = DatabaseConfig.getMysqlServerId();
        return configured == null || configured.isBlank() ? "default" : configured;
    }

    private static String cooldownKey(String configFile, String location) {
        return configFile + "\0" + locationKey(location);
    }

    @FunctionalInterface
    interface SqlOperation {
        void execute(Connection connection) throws Exception;
    }

    record RuntimeWrite(String description, SqlOperation operation) {
    }
}
