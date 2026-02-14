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
                "Currency DOUBLE, " +
                "QuestStatus BLOB, " +
                "Score INT, " +
                "Kills INT, " +
                "HighestLevelKilled INT, " +
                "Deaths INT, " +
                "QuestsCompleted INT, " +
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
                "SkillBonusSelections BLOB, " +
                "GamblingDebt DOUBLE" +
                ");";
        statement.executeUpdate(sql);
        statement.close();

        // Check and add missing columns if any
        addEntryIfEmpty("DisplayName", ColumnValues.TEXT);
        addEntryIfEmpty("Currency", ColumnValues.REAL);
        addEntryIfEmpty("QuestStatus", ColumnValues.BLOB);
        addEntryIfEmpty("Score", ColumnValues.INT);
        addEntryIfEmpty("Kills", ColumnValues.INT);
        addEntryIfEmpty("HighestLevelKilled", ColumnValues.INT);
        addEntryIfEmpty("Deaths", ColumnValues.INT);
        addEntryIfEmpty("QuestsCompleted", ColumnValues.INT);
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

        // Skill bonus selections (JSON)
        addEntryIfEmpty("SkillBonusSelections", ColumnValues.BLOB);

        // Gambling debt
        addEntryIfEmpty("GamblingDebt", ColumnValues.REAL);
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
