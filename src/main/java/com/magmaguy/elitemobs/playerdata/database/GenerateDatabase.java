package com.magmaguy.elitemobs.playerdata.database;

import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

public class GenerateDatabase {
    private GenerateDatabase() {
    }

    public static void generate() throws Exception {
        Statement statement = PlayerData.getConnection().createStatement();
       //String sql = "CREATE TABLE IF NOT EXISTS " + PlayerData.getPLAYER_DATA_TABLE_NAME() +
       //        "(PlayerUUID             TEXT PRIMARY KEY    NOT NULL," +
       //        " DisplayName                       TEXT," +
       //        " Currency                          REAL," +
       //        " GuildPrestigeLevel                 INT," +
       //        " GuildMaxLevel                      INT," +
       //        " GuildActiveLevel                   INT," +
       //        " QuestStatus                       BLOB," +
       //        " Score                              INT," +
       //        " Kills                              INT," +
       //        " HighestLevelKilled                 INT," +
       //        " Deaths                             INT," +
       //        " QuestsCompleted                    INT," +
       //        " PlayerQuestCooldowns               BLOB);";
        String sql = "CREATE TABLE IF NOT EXISTS " + PlayerData.getPLAYER_DATA_TABLE_NAME() +
                "(PlayerUUID             TEXT PRIMARY KEY    NOT NULL);" ;
        statement.executeUpdate(sql);
        statement.close();
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
    }

    private static void addEntryIfEmpty(String columnName, ColumnValues columnValues){
        try {
            DatabaseMetaData metaData = PlayerData.getConnection().getMetaData();
            ResultSet resultSet = metaData.getColumns(null, null, PlayerData.getPLAYER_DATA_TABLE_NAME(), columnName);
            if (resultSet.next()) {
               //Developer.message("Database already had " + columnName);
            } else {
                new InfoMessage("Adding new database column " + columnName);
                addColumn(columnName, columnValues);
            }
            resultSet.close();
        } catch (Exception ex) {
            new WarningMessage("Could not process column " + columnName);
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
            new WarningMessage("Failed to insert new column " + columnName);
            ex.printStackTrace();
        }
    }

    private enum ColumnValues {
        BLOB,
        INT,
        TEXT,
        REAL
    }

}
