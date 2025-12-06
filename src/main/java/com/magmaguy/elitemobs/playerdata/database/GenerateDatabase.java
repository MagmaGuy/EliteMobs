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
                "GuildPrestigeLevel INT, " +
                "GuildMaxLevel INT, " +
                "GuildActiveLevel INT, " +
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
                "DungeonBossLockouts BLOB" +
                ");";
        statement.executeUpdate(sql);
        statement.close();

        // Check and add missing columns if any
        addEntryIfEmpty("DisplayName", ColumnValues.TEXT);
        addEntryIfEmpty("Currency", ColumnValues.REAL);
        addEntryIfEmpty("GuildPrestigeLevel", ColumnValues.INT);
        addEntryIfEmpty("GuildMaxLevel", ColumnValues.INT);
        addEntryIfEmpty("GuildActiveLevel", ColumnValues.INT);
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
        TEXT,
        REAL,
        BOOLEAN
    }

}
