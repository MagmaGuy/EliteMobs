package com.magmaguy.elitemobs.playerdata.database;

import com.magmaguy.elitemobs.config.DatabaseConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DungeonRuntimeDataTest {

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        DatabaseConfig.mysqlServerId = "test-server";
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        DungeonRuntimeData.initializeSchema(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void cooldownUpsertKeepsOneLatestRow() throws Exception {
        String location = "world,1,2,3,0,0";

        DungeonRuntimeData.upsertCooldown(connection, DungeonRuntimeData.REGIONAL_BOSS_TABLE,
                "RespawnAt", "boss.yml", location, 100L);
        DungeonRuntimeData.upsertCooldown(connection, DungeonRuntimeData.REGIONAL_BOSS_TABLE,
                "RespawnAt", "boss.yml", location, 200L);

        assertEquals(200L, DungeonRuntimeData.readCooldown(connection,
                DungeonRuntimeData.REGIONAL_BOSS_TABLE, "RespawnAt", "boss.yml", location));
        assertEquals(1, rowCount(DungeonRuntimeData.REGIONAL_BOSS_TABLE));
    }

    @Test
    void playerCooldownUpsertKeepsOneLatestRow() throws Exception {
        UUID playerId = UUID.randomUUID();

        DungeonRuntimeData.upsertPlayerCooldown(connection, "chest.yml", playerId, 100L);
        DungeonRuntimeData.upsertPlayerCooldown(connection, "chest.yml", playerId, 200L);

        assertEquals(playerId + ":200", DungeonRuntimeData.readPlayerCooldowns(connection, "chest.yml").getFirst());
        assertEquals(1, rowCount(DungeonRuntimeData.TREASURE_CHEST_PLAYER_TABLE));
    }

    @Test
    void missingLegacyPlayerCooldownsLoadAsMutableEmptyListWhenDatabaseIsUnavailable() {
        DungeonRuntimeData.shutdown();

        List<String> cooldowns = DungeonRuntimeData.loadTreasureChestPlayerCooldowns("chest.yml", null);
        cooldowns.add(UUID.randomUUID() + ":200");

        assertEquals(1, cooldowns.size());
    }

    @Test
    void runtimeNamespacesDoNotShareCooldowns() throws Exception {
        String location = "world,1,2,3,0,0";
        DungeonRuntimeData.upsertCooldown(connection, DungeonRuntimeData.REGIONAL_BOSS_TABLE,
                "RespawnAt", "boss.yml", location, 100L);

        DatabaseConfig.mysqlServerId = "other-server";

        assertNull(DungeonRuntimeData.readCooldown(connection,
                DungeonRuntimeData.REGIONAL_BOSS_TABLE, "RespawnAt", "boss.yml", location));
    }

    @Test
    void legacyRuntimeTableGetsNamespaceColumnWithoutLosingRows() throws Exception {
        connection.close();
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE RegionalBossCooldowns ("
                    + "ConfigFile VARCHAR(255) NOT NULL, LocationKey CHAR(64) NOT NULL, RespawnAt BIGINT NOT NULL,"
                    + " PRIMARY KEY (ConfigFile, LocationKey))");
            statement.executeUpdate("INSERT INTO RegionalBossCooldowns VALUES ('boss.yml', 'hash', 321)");
        }

        DungeonRuntimeData.initializeSchema(connection);

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT ServerId, RespawnAt FROM RegionalBossCooldowns")) {
            resultSet.next();
            assertEquals("test-server", resultSet.getString("ServerId"));
            assertEquals(321L, resultSet.getLong("RespawnAt"));
        }
    }

    private int rowCount(String table) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}
