package com.magmaguy.elitemobs.playerdata;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.quests.PlayerQuests;
import com.magmaguy.elitemobs.utils.DebugMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

public class PlayerData {

    private static final HashMap<UUID, PlayerData> playerDataHashMap = new HashMap<>();

    public static void clearPlayerData(UUID uuid) {
        playerDataHashMap.remove(uuid);
    }

    public static boolean isInMemory(Player player) {
        return playerDataHashMap.containsKey(player.getUniqueId());
    }

    public static boolean isInMemory(UUID uuid) {
        return playerDataHashMap.containsKey(uuid);
    }

    public static String getDisplayName(UUID uuid) {
        if (!isInMemory(uuid))
            return getDatabaseString(uuid, "DisplayName");

        return Bukkit.getPlayer(uuid).getCustomName();
    }

    public static void setDisplayName(UUID uuid, String displayName) {
        setDatabaseValue(uuid, "DisplayName", displayName);
    }

    public static double getCurrency(UUID uuid) {
        if (!isInMemory(uuid))
            return getDatabaseDouble(uuid, "Currency");
        return playerDataHashMap.get(uuid).currency;
    }

    public static void setCurrency(UUID uuid, double currency) {
        setDatabaseValue(uuid, "Currency", currency);
        if (playerDataHashMap.containsKey(uuid))
            playerDataHashMap.get(uuid).currency = currency;
    }

    public static int getGuildPrestigeLevel(UUID uuid) {
        if (!isInMemory(uuid))
            return getDatabaseInteger(uuid, "GuildPrestigeLevel");
        return playerDataHashMap.get(uuid).guildPrestigeLevel;
    }

    public static void setGuildPrestigeLevel(UUID uuid, int newPrestigeLevel) {
        setDatabaseValue(uuid, "GuildPrestigeLevel", newPrestigeLevel);
        if (playerDataHashMap.containsKey(uuid))
            playerDataHashMap.get(uuid).guildPrestigeLevel = newPrestigeLevel;
    }

    public static int getMaxGuildLevel(UUID uuid) {
        if (!isInMemory(uuid))
            return getDatabaseInteger(uuid, "GuildMaxLevel");

        return playerDataHashMap.get(uuid).maxGuildLevel;
    }

    public static void setMaxGuildLevel(UUID uuid, int maxGuildLevel) {
        setDatabaseValue(uuid, "GuildMaxLevel", maxGuildLevel);

        if (playerDataHashMap.containsKey(uuid))
            playerDataHashMap.get(uuid).maxGuildLevel = maxGuildLevel;
    }

    public static int getActiveGuildLevel(UUID uuid) {
        if (!isInMemory(uuid))
            return getDatabaseInteger(uuid, "GuildActiveLevel");

        return playerDataHashMap.get(uuid).activeGuildLevel;
    }

    public static void setActiveGuildLevel(UUID uuid, int activeGuildLevel) {
        setDatabaseValue(uuid, "GuildActiveLevel", activeGuildLevel);

        if (playerDataHashMap.containsKey(uuid))
            playerDataHashMap.get(uuid).activeGuildLevel = activeGuildLevel;
    }

    public static PlayerQuests getQuestStatus(UUID uuid) {
        try {
            if (!isInMemory(uuid))
                return (PlayerQuests) getDatabaseBlob(uuid, "QuestStatus");
            return playerDataHashMap.get(uuid).questStatus;
        } catch (Exception ex) {
            return null;
        }
    }

    public static void setQuestStatus(UUID uuid, PlayerQuests questStatus) {
        //todo: proper serialization
        try {
            setDatabaseValue(uuid, "QuestStatus", questStatus);
            if (playerDataHashMap.containsKey(uuid))
                playerDataHashMap.get(uuid).questStatus = questStatus;
        } catch (Exception ex) {
            new WarningMessage("Failed to serialize player quest data!");
        }
    }

    public static int getScore(UUID uuid) {
        if (!isInMemory(uuid))
            return getDatabaseInteger(uuid, "Score");

        return playerDataHashMap.get(uuid).score;
    }

    public static void incrementScore(UUID uuid, int levelToIncrement) {
        setDatabaseValue(uuid, "Score", getScore(uuid) + levelToIncrement * 2);

        if (playerDataHashMap.containsKey(uuid))
            playerDataHashMap.get(uuid).score += (levelToIncrement * 2);
    }

    public static void decrementScore(UUID uuid) {
        setDatabaseValue(uuid, "Score", getScore(uuid) - 100);

        if (playerDataHashMap.containsKey(uuid))
            playerDataHashMap.get(uuid).score -= 100;
    }

    public static int getKills(UUID uuid) {
        if (!isInMemory(uuid))
            return getDatabaseInteger(uuid, "Kills");

        return playerDataHashMap.get(uuid).kills;
    }

    public static void incrementKills(UUID uuid) {
        setDatabaseValue(uuid, "Kills", getKills(uuid) + 1);

        if (playerDataHashMap.containsKey(uuid))
            playerDataHashMap.get(uuid).kills += 1;
    }

    public static int getHighestLevelKilled(UUID uuid) {
        if (!isInMemory(uuid))
            return getDatabaseInteger(uuid, "HighestLevelKilled");

        return playerDataHashMap.get(uuid).highestLevelKilled;
    }

    public static void setHighestLevelKilled(UUID uuid, int tentativeNewLevel) {
        if (tentativeNewLevel < getHighestLevelKilled(uuid)) return;
        setDatabaseValue(uuid, "HighestLevelKilled", tentativeNewLevel);

        if (playerDataHashMap.containsKey(uuid))
            playerDataHashMap.get(uuid).highestLevelKilled = tentativeNewLevel;
    }

    public static int getDeaths(UUID uuid) {
        if (!isInMemory(uuid))
            return getDatabaseInteger(uuid, "Deaths");

        return playerDataHashMap.get(uuid).deaths;
    }

    public static void incrementDeaths(UUID uuid) {
        setDatabaseValue(uuid, "Deaths", getDeaths(uuid) + 1);

        if (playerDataHashMap.containsKey(uuid))
            playerDataHashMap.get(uuid).deaths += 1;
    }

    public static int getQuestsCompleted(UUID uuid) {
        if (!isInMemory(uuid))
            return getDatabaseInteger(uuid, "QuestsCompleted");

        return playerDataHashMap.get(uuid).questsCompleted;
    }

    public static void incrementQuestsCompleted(UUID uuid) {
        setDatabaseValue(uuid, "QuestsCompleted", getQuestsCompleted(uuid) + 1);

        if (playerDataHashMap.containsKey(uuid))
            playerDataHashMap.get(uuid).questsCompleted += 1;
    }

    public static void setDatabaseValue(UUID uuid, String key, Object value) {
        Statement statement = null;

        try {
            getConnection().setAutoCommit(false);
            statement = connection.createStatement();
            String sql;
            if (value instanceof String)
                sql = "UPDATE " + player_data_table_name + " SET " + key + " = '" + value + "' WHERE PlayerUUID = '" + uuid.toString() + "';";
            else
                sql = "UPDATE " + player_data_table_name + " SET " + key + " = " + value + " WHERE PlayerUUID = '" + uuid.toString() + "';";
            statement.executeUpdate(sql);
            getConnection().commit();

            statement.close();
            getConnection().close();
        } catch (Exception e) {
            new WarningMessage("Failed to update database value.");
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            closeConnection();
        }
    }

    private static Object getDatabaseBlob(UUID uuid, String value) {
        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + player_data_table_name + " WHERE PlayerUUID = '" + uuid.toString() + "';");
            Object blob = resultSet.getBlob(value);
            resultSet.close();
            statement.close();
            getConnection().close();
            return blob;
        } catch (Exception e) {
            new WarningMessage("Failed to get string value from database!");
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            closeConnection();
            return null;
        }
    }

    private static String getDatabaseString(UUID uuid, String value) {
        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + player_data_table_name + " WHERE PlayerUUID = '" + uuid.toString() + "';");
            String reply = resultSet.getString(value);
            resultSet.close();
            statement.close();
            getConnection().close();
            return reply;
        } catch (Exception e) {
            new WarningMessage("Failed to get string value from database!");
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            closeConnection();
            return null;
        }
    }

    private static Double getDatabaseDouble(UUID uuid, String value) {
        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + player_data_table_name + " WHERE PlayerUUID = '" + uuid.toString() + "';");
            double reply = resultSet.getDouble(value);
            resultSet.close();
            statement.close();
            getConnection().close();
            return reply;
        } catch (Exception e) {
            new WarningMessage("Failed to get double value from database!");
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            closeConnection();
            return null;
        }
    }

    private static Integer getDatabaseInteger(UUID uuid, String value) {
        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + player_data_table_name + " WHERE PlayerUUID = '" + uuid.toString() + "';");
            int reply = resultSet.getInt(value);
            resultSet.close();
            statement.close();
            getConnection().close();
            return reply;
        } catch (Exception e) {
            new WarningMessage("Failed to get integer value from database!");
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            closeConnection();
            return null;
        }
    }

    private static ResultSet getResultSet(UUID uuid) {
        Statement statement = null;
        try {
            getConnection().setAutoCommit(false);
            //statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + player_data_table_name + " WHERE PlayerUUID = '" + uuid.toString() + "';");

            //statement.close();
            //getConnection().close();
            return resultSet;

        } catch (Exception e) {
            new WarningMessage("Failed to get value from database!");
            closeConnection();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return null;
        }
    }

    private double currency;
    private int guildPrestigeLevel, maxGuildLevel, activeGuildLevel, score, kills, highestLevelKilled, deaths, questsCompleted;
    private PlayerQuests questStatus;

    /**
     * Called when a player logs in, storing their data in memory
     *
     * @param uuid
     */
    public PlayerData(UUID uuid) {
        Statement statement = null;
        closeConnection();
        try {
            getConnection().setAutoCommit(false);
            statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + player_data_table_name + " WHERE PlayerUUID = '" + uuid.toString() + "';");

            this.currency = resultSet.getDouble("Currency");
            this.guildPrestigeLevel = resultSet.getInt("GuildPrestigeLevel");
            this.maxGuildLevel = resultSet.getInt("GuildMaxLevel");
            this.activeGuildLevel = resultSet.getInt("GuildActiveLevel");
            this.score = resultSet.getInt("Score");
            try {
                this.questStatus = (PlayerQuests) resultSet.getBlob("QuestStatus");
            } catch (Exception exception) {
                //for players with no quest status
                questStatus = new PlayerQuests(Bukkit.getPlayer(uuid));
            }
            this.kills = resultSet.getInt("Kills");
            this.highestLevelKilled = resultSet.getInt("HighestLevelKilled");
            this.deaths = resultSet.getInt("Deaths");
            this.questsCompleted = resultSet.getInt("QuestsCompleted");

            playerDataHashMap.put(uuid, this);

            resultSet.close();
            statement.close();
            getConnection().close();
            new DebugMessage("Loaded data from player " + Bukkit.getPlayer(uuid).getDisplayName());
            return;
        } catch (Exception e) {
            new WarningMessage("No player entry detected, generating new entry!");
            closeConnection();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        try {
            getConnection().setAutoCommit(false);

            statement = getConnection().createStatement();
            String sql = "INSERT INTO " + player_data_table_name +
                    " (PlayerUUID," +
                    " DisplayName," +
                    " Currency," +
                    " GuildPrestigeLevel," +
                    " GuildMaxLevel," +
                    " GuildActiveLevel," +
                    " Score," +
                    " Kills," +
                    " HighestLevelKilled," +
                    " Deaths," +
                    " QuestsCompleted) " +
                    //identifier
                    "VALUES ('" + uuid.toString() + "'," +
                    //display name
                    " '" + Bukkit.getPlayer(uuid).getName() + "'," +
                    //currency
                    " 0," +
                    //guild_prestige_level
                    " 0," +
                    //guild_max_level
                    " 1," +
                    //guild_active_level
                    " 1," +
                    //score
                    "0," +
                    //kills
                    "0," +
                    //highestLevelKilled
                    "0," +
                    //deaths
                    "0," +
                    //questsCompleted
                    "0);";
            statement.executeUpdate(sql);

            this.currency = 0;
            this.guildPrestigeLevel = 0;
            this.maxGuildLevel = 1;
            this.activeGuildLevel = 1;
            this.questStatus = null;
            this.score = 0;
            this.kills = 0;
            this.highestLevelKilled = 0;
            this.deaths = 0;
            this.questsCompleted = 0;

            playerDataHashMap.put(uuid, this);

            statement.close();
            getConnection().commit();
            getConnection().close();
            new DebugMessage("Created new database entry for player " + Bukkit.getPlayer(uuid).getDisplayName());
        } catch (Exception e) {
            new WarningMessage("Failed to generate an entry!");
            closeConnection();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private static Connection connection = null;
    public static final String database_name = "player_data.db";
    public static final String player_data_table_name = "PlayerData";

    public static Connection getConnection() throws Exception {
        File dataFolder = new File(MetadataHandler.PLUGIN.getDataFolder(), "data/" + database_name);
        if (connection == null || connection.isClosed()) {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection == null) return;
            connection.close();
        } catch (Exception ex) {
            new WarningMessage("Could not correctly close database connection.");
        }
    }

    public static void initializeDatabaseConnection() {
        new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/data").mkdirs();
        Statement statement = null;
        try {
            System.out.println("Opened database successfully");

            statement = getConnection().createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS " + player_data_table_name +
                    "(PlayerUUID             TEXT PRIMARY KEY    NOT NULL," +
                    " DisplayName                       TEXT," +
                    " Currency                          REAL," +
                    " GuildPrestigeLevel                 INT," +
                    " GuildMaxLevel                      INT," +
                    " GuildActiveLevel                   INT," +
                    " QuestStatus                       BLOB," +
                    " Score                              INT," +
                    " Kills                              INT," +
                    " HighestLevelKilled                 INT," +
                    " Deaths                             INT," +
                    " QuestsCompleted                    INT);";
            statement.executeUpdate(sql);
            statement.close();
            getConnection().close();

            for (Player player : Bukkit.getOnlinePlayers())
                new PlayerData(player.getUniqueId());

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            new WarningMessage("Failed to establish a connection to the SQLite database. This is not good.");
            closeConnection();
        }

        new PortOldData();

    }

    public static class PlayerDataEvents implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerLogin(PlayerJoinEvent event) {
            new PlayerData(event.getPlayer().getUniqueId());
        }

        @EventHandler
        public void onPlayerLogout(PlayerQuitEvent event) {
            clearPlayerData(event.getPlayer().getUniqueId());
            setDisplayName(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        }
    }

}
