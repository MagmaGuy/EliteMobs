package com.magmaguy.elitemobs.playerdata.database;

import com.magmaguy.magmacore.util.Logger;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

public class GenerateDatabase {
    private GenerateDatabase() {
    }

    public static void generate() throws Exception {
        Statement statement = PlayerData.getConnection().createStatement();
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
        statement.close();

        createExperimentalCombatTables();

        // Check and add missing columns if any
        addEntryIfEmpty("DisplayName", ColumnValues.TEXT);
        addEntryIfEmpty("CurrencyV2", ColumnValues.REAL);
        addEntryIfEmpty("CurrencyCents", ColumnValues.BIGINT);
        addEntryIfEmpty("QuestStatus", ColumnValues.BLOB);
        addEntryIfEmpty("Score", ColumnValues.INT);
        addEntryIfEmpty("Kills", ColumnValues.INT);
        addEntryIfEmpty("HighestLevelKilled", ColumnValues.INT);
        addEntryIfEmpty("Deaths", ColumnValues.INT);
        addEntryIfEmpty("QuestsCompleted", ColumnValues.INT);
        addEntryIfEmpty("DungeonsCompleted", ColumnValues.INT);
        addEntryIfEmpty("PlayerQuestCooldowns", ColumnValues.BLOB);
        addEntryIfEmpty("BackTeleportLocation", ColumnValues.TEXT);
        addEntryIfEmpty("UseBookMenus", ColumnValues.BOOLEAN);
        addEntryIfEmpty("DismissEMStatusScreenMessage", ColumnValues.BOOLEAN);
        addEntryIfEmpty("DungeonBossLockouts", ColumnValues.BLOB);
        addEntryIfEmpty("QuestLockouts", ColumnValues.BLOB);

        // Skill XP columns
        addEntryIfEmpty("SkillXP_ARMOR", ColumnValues.BIGINT);
        addEntryIfEmpty("SkillXP_SWORDS", ColumnValues.BIGINT);
        addEntryIfEmpty("SkillXP_AXES", ColumnValues.BIGINT);
        addEntryIfEmpty("SkillXP_BOWS", ColumnValues.BIGINT);
        addEntryIfEmpty("SkillXP_CROSSBOWS", ColumnValues.BIGINT);
        addEntryIfEmpty("SkillXP_TRIDENTS", ColumnValues.BIGINT);
        addEntryIfEmpty("SkillXP_HOES", ColumnValues.BIGINT);
        addEntryIfEmpty("SkillXP_MACES", ColumnValues.BIGINT);
        addEntryIfEmpty("SkillXP_SPEARS", ColumnValues.BIGINT);
        addEntryIfEmpty("SkillXP_STAVES", ColumnValues.BIGINT);
        addEntryIfEmpty("SkillXP_WANDS", ColumnValues.BIGINT);

        // Skill bonus selections (JSON)
        addEntryIfEmpty("SkillBonusSelections", ColumnValues.BLOB);

        // Gambling debt
        addEntryIfEmpty("GamblingDebt", ColumnValues.REAL);
        addEntryIfEmpty("GamblingDebtCents", ColumnValues.BIGINT);
    }

    private static void createExperimentalCombatTables() throws Exception {
        synchronized (PlayerDataRepository.monitor()) {
            try (Statement statement = PlayerDataRepository.connection().createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS "
                        + JdbcClassProgressionStore.PROFILE_TABLE + " ("
                        + "PlayerUUID VARCHAR(36) PRIMARY KEY NOT NULL, "
                        + "SelectedFormId VARCHAR(64), "
                        + "SelectedInputId VARCHAR(64), "
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

    private static void addEntryIfEmpty(String columnName, ColumnValues columnValues) {
        try {
            DatabaseMetaData metaData = PlayerData.getConnection().getMetaData();
            ResultSet resultSet = metaData.getColumns(null, null, PlayerData.getPLAYER_DATA_TABLE_NAME(), columnName);
            if (resultSet.next()) {
                //Logger.message("Database already had " + columnName);
            } else {
                Logger.info("Adding new database column " + columnName);
                addColumn(columnName, columnValues);
            }
            resultSet.close();
        } catch (Exception ex) {
            Logger.warn("Could not process column " + columnName);
            ex.printStackTrace();
        }
    }

    private static void addColumn(String columnName, ColumnValues type) {
        try {
            Statement statement = PlayerData.getConnection().createStatement();
            String sql = "ALTER TABLE " + PlayerData.getPLAYER_DATA_TABLE_NAME() + " ADD " + columnName + " " + type.toString();
            statement.executeUpdate(sql);
            statement.close();
        } catch (Exception ex) {
            Logger.warn("Failed to insert new column " + columnName);
            ex.printStackTrace();
        }
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
