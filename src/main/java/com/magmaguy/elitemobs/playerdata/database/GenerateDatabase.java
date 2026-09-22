package com.magmaguy.elitemobs.playerdata.database;

import com.magmaguy.magmacore.util.Logger;

import java.sql.ResultSet;
import java.sql.Statement;

public class GenerateDatabase {
    private GenerateDatabase() {
    }

    public static void generate() throws Exception {
        synchronized (PlayerDataRepository.jdbcMonitor()) {
            generateLocked();
        }
    }

    private static void generateLocked() throws Exception {
        try (Statement statement = PlayerData.getConnection().createStatement()) {
        // Create table with all columns defined
        String sql = "CREATE TABLE IF NOT EXISTS " + PlayerData.getPLAYER_DATA_TABLE_NAME() + " (" +
                "PlayerUUID VARCHAR(36) PRIMARY KEY NOT NULL, " +
                "DisplayName TEXT, " +
                "CurrencyV2 DOUBLE, " +
                "CurrencyCents BIGINT, " +
                "QuestStatus BLOB, " +
                "Score INT, " +
                "Kills INT, " +
                "HighestLevelKilled INT, " +
                "Deaths INT, " +
                "QuestsCompleted INT, " +
                "DungeonsCompleted INT, " +
                "PlayerQuestCooldowns BLOB, " +
                "BackTeleportLocation TEXT, " +
                "UseBookMenus TINYINT(1), " +
                "DismissEMStatusScreenMessage TINYINT(1), " +
                "DungeonBossLockouts BLOB, " +
                "QuestLockouts BLOB, " +
                "SkillXP_ARMOR BIGINT, " +
                "SkillXP_SWORDS BIGINT, " +
                "SkillXP_AXES BIGINT, " +
                "SkillXP_BOWS BIGINT, " +
                "SkillXP_CROSSBOWS BIGINT, " +
                "SkillXP_TRIDENTS BIGINT, " +
                "SkillXP_HOES BIGINT, " +
                "SkillXP_MACES BIGINT, " +
                "SkillXP_SPEARS BIGINT, " +
                "SkillXP_STAVES BIGINT, " +
                "SkillXP_WANDS BIGINT, " +
                "SkillBonusSelections BLOB, " +
                "GamblingDebt DOUBLE, " +
                "GamblingDebtCents BIGINT" +
                ");";
        statement.executeUpdate(sql);
        }
        createAdvancedCombatTables();
        java.util.Set<String> columns = new java.util.HashSet<>();
        try (ResultSet existing = PlayerData.getConnection().getMetaData().getColumns(
                null, null, PlayerData.getPLAYER_DATA_TABLE_NAME(), null)) {
            while (existing.next()) columns.add(existing.getString("COLUMN_NAME").toLowerCase(java.util.Locale.ROOT));
        }

        // Check and add missing columns if any
        addEntryIfEmpty(columns, "DisplayName", ColumnValues.TEXT);
        addEntryIfEmpty(columns, "CurrencyV2", ColumnValues.REAL);
        addEntryIfEmpty(columns, "CurrencyCents", ColumnValues.BIGINT);
        addEntryIfEmpty(columns, "QuestStatus", ColumnValues.BLOB);
        addEntryIfEmpty(columns, "Score", ColumnValues.INT);
        addEntryIfEmpty(columns, "Kills", ColumnValues.INT);
        addEntryIfEmpty(columns, "HighestLevelKilled", ColumnValues.INT);
        addEntryIfEmpty(columns, "Deaths", ColumnValues.INT);
        addEntryIfEmpty(columns, "QuestsCompleted", ColumnValues.INT);
        addEntryIfEmpty(columns, "DungeonsCompleted", ColumnValues.INT);
        addEntryIfEmpty(columns, "PlayerQuestCooldowns", ColumnValues.BLOB);
        addEntryIfEmpty(columns, "BackTeleportLocation", ColumnValues.TEXT);
        addEntryIfEmpty(columns, "UseBookMenus", ColumnValues.BOOLEAN);
        addEntryIfEmpty(columns, "DismissEMStatusScreenMessage", ColumnValues.BOOLEAN);
        addEntryIfEmpty(columns, "DungeonBossLockouts", ColumnValues.BLOB);
        addEntryIfEmpty(columns, "QuestLockouts", ColumnValues.BLOB);

        // Skill XP columns
        addEntryIfEmpty(columns, "SkillXP_ARMOR", ColumnValues.BIGINT);
        addEntryIfEmpty(columns, "SkillXP_SWORDS", ColumnValues.BIGINT);
        addEntryIfEmpty(columns, "SkillXP_AXES", ColumnValues.BIGINT);
        addEntryIfEmpty(columns, "SkillXP_BOWS", ColumnValues.BIGINT);
        addEntryIfEmpty(columns, "SkillXP_CROSSBOWS", ColumnValues.BIGINT);
        addEntryIfEmpty(columns, "SkillXP_TRIDENTS", ColumnValues.BIGINT);
        addEntryIfEmpty(columns, "SkillXP_HOES", ColumnValues.BIGINT);
        addEntryIfEmpty(columns, "SkillXP_MACES", ColumnValues.BIGINT);
        addEntryIfEmpty(columns, "SkillXP_SPEARS", ColumnValues.BIGINT);
        addEntryIfEmpty(columns, "SkillXP_STAVES", ColumnValues.BIGINT);
        addEntryIfEmpty(columns, "SkillXP_WANDS", ColumnValues.BIGINT);

        // Skill bonus selections (JSON)
        addEntryIfEmpty(columns, "SkillBonusSelections", ColumnValues.BLOB);

        // Gambling debt
        addEntryIfEmpty(columns, "GamblingDebt", ColumnValues.REAL);
        addEntryIfEmpty(columns, "GamblingDebtCents", ColumnValues.BIGINT);
    }

    private static void createAdvancedCombatTables() throws Exception {
        synchronized (PlayerDataRepository.jdbcMonitor()) {
            try (Statement statement = PlayerDataRepository.connection().createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS "
                        + JdbcClassProgressionStore.PROFILE_TABLE + " ("
                        + "PlayerUUID VARCHAR(36) PRIMARY KEY NOT NULL, "
                        + "SelectedFormId VARCHAR(64), "
                        + "SelectedInputId VARCHAR(64), "
                        + "TutorialSkillsUsed INTEGER NOT NULL DEFAULT 0, "
                        + "CatalogVersion INTEGER NOT NULL DEFAULT 0"
                        + ")");
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS "
                        + JdbcClassProgressionStore.PROGRESS_TABLE + " ("
                        + "PlayerUUID VARCHAR(36) NOT NULL, "
                        + "FormId VARCHAR(64) NOT NULL, "
                        + "XP BIGINT NOT NULL DEFAULT 0, "
                        + "ChallengeCompleted INTEGER NOT NULL DEFAULT 0, "
                        + "CatalogVersion INTEGER NOT NULL DEFAULT 0, "
                        + "PRIMARY KEY (PlayerUUID, FormId)"
                        + ")");
                boolean hasTutorialColumn;
                try (var columns = PlayerDataRepository.connection().getMetaData().getColumns(
                        null, null, JdbcClassProgressionStore.PROFILE_TABLE, "TutorialSkillsUsed")) {
                    hasTutorialColumn = columns.next();
                }
                if (!hasTutorialColumn)
                    statement.executeUpdate("ALTER TABLE " + JdbcClassProgressionStore.PROFILE_TABLE
                            + " ADD TutorialSkillsUsed INTEGER NOT NULL DEFAULT 0");
                boolean hasChallengeColumn;
                try (var columns = PlayerDataRepository.connection().getMetaData().getColumns(
                        null, null, JdbcClassProgressionStore.PROGRESS_TABLE, "ChallengeCompleted")) {
                    hasChallengeColumn = columns.next();
                }
                // One DDL operation preserves legacy rows even if startup is interrupted.
                // New progress inserts always bind their explicit completion flag.
                if (!hasChallengeColumn)
                    statement.executeUpdate("ALTER TABLE " + JdbcClassProgressionStore.PROGRESS_TABLE
                            + " ADD ChallengeCompleted INTEGER NOT NULL DEFAULT 1");
            }
        }
    }

    private static void addEntryIfEmpty(java.util.Set<String> columns, String columnName,
                                        ColumnValues type) throws Exception {
        if (columns.contains(columnName.toLowerCase(java.util.Locale.ROOT))) return;
        Logger.info("Adding new database column " + columnName);
        try (Statement statement = PlayerData.getConnection().createStatement()) {
            statement.executeUpdate("ALTER TABLE " + PlayerData.getPLAYER_DATA_TABLE_NAME()
                    + " ADD " + columnName + " " + type);
        }
        columns.add(columnName.toLowerCase(java.util.Locale.ROOT));
    }

    private enum ColumnValues {
        BLOB,
        INT,
        BIGINT,
        TEXT,
        REAL,
        BOOLEAN
    }

}
