package com.magmaguy.elitemobs.playerdata;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.quests.EliteQuest;
import com.magmaguy.elitemobs.quests.PlayerQuests;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
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

    public static double getCurrency(UUID uuid, boolean databaseAccess) {
        if (!isInMemory(uuid))
            if (databaseAccess)
                return getDatabaseDouble(uuid, "Currency");
            else
                //default fallback value for when PAPI suddenly decides to start querying the database thousands of times per second
                return 0;
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

    public static int getGuildPrestigeLevel(UUID uuid, boolean databaseAccess) {
        if (!isInMemory(uuid))
            if (databaseAccess)
                return getDatabaseInteger(uuid, "GuildPrestigeLevel");
            else
                //default fallback value for when PAPI suddenly decides to start querying the database thousands of times per second
                return 0;
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

    public static int getMaxGuildLevel(UUID uuid, boolean databaseAccess) {
        if (!isInMemory(uuid))
            if (databaseAccess)
                return getDatabaseInteger(uuid, "GuildMaxLevel");
            else
                //default fallback value for when PAPI suddenly decides to start querying the database thousands of times per second
                return 0;

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

    public static int getActiveGuildLevel(UUID uuid, boolean databaseAccess) {
        if (!isInMemory(uuid))
            if (databaseAccess)
                return getDatabaseInteger(uuid, "GuildActiveLevel");
            else
                //default fallback value for when PAPI suddenly decides to start querying the database thousands of times per second
                return 0;

        return playerDataHashMap.get(uuid).activeGuildLevel;
    }

    public static void setActiveGuildLevel(UUID uuid, int activeGuildLevel) {
        setDatabaseValue(uuid, "GuildActiveLevel", activeGuildLevel);

        if (playerDataHashMap.containsKey(uuid))
            playerDataHashMap.get(uuid).activeGuildLevel = activeGuildLevel;
    }

    public static PlayerQuests getQuestStatus(UUID uuid) {
        try {
            //todo: store quest progress long-term
            //if (!isInMemory(uuid))
            //return (PlayerQuests) getDatabaseBlob(uuid, "QuestStatus");
            return playerDataHashMap.get(uuid).questStatus;
        } catch (Exception ex) {
            return null;
        }
    }

    public static void removeQuest(UUID uuid, UUID questUUID) {
        //todo: better handling, tie in to db
        EliteQuest eliteQuest = null;
        for (EliteQuest eliteQuest1 : playerDataHashMap.get(uuid).questStatus.quests)
            if (eliteQuest1.getUuid().equals(questUUID))
                eliteQuest = eliteQuest1;
        playerDataHashMap.get(uuid).questStatus.quests.remove(eliteQuest);
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

    private final boolean update = false;

    public static void setDatabaseValue(UUID uuid, String key, Object value) {

        new BukkitRunnable() {
            @Override
            public void run() {
                Statement statement = null;
                try {
                    statement = getConnection().createStatement();
                    String sql;
                    if (value instanceof String)
                        sql = "UPDATE " + player_data_table_name + " SET " + key + " = '" + value + "' WHERE PlayerUUID = '" + uuid.toString() + "';";
                    else
                        sql = "UPDATE " + player_data_table_name + " SET " + key + " = " + value + " WHERE PlayerUUID = '" + uuid.toString() + "';";
                    statement.executeUpdate(sql);
                    statement.close();

                } catch (Exception e) {
                    new WarningMessage("Failed to update database value.");
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(MetadataHandler.PLUGIN);

    }

    private static Object getDatabaseBlob(UUID uuid, String value) {
        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + player_data_table_name + " WHERE PlayerUUID = '" + uuid.toString() + "';");
            Object blob = resultSet.getBlob(value);
            resultSet.close();
            statement.close();
            return blob;
        } catch (Exception e) {
            new WarningMessage("Failed to get string value from database!");
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
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
            return reply;
        } catch (Exception e) {
            new WarningMessage("Failed to get string value from database!");
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
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
            return reply;
        } catch (Exception e) {
            new WarningMessage("Failed to get double value from database!");
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
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
            return reply;
        } catch (Exception e) {
            new WarningMessage("Failed to get integer value from database!");
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
        PlayerData playerData = this;
        new BukkitRunnable() {
            @Override
            public void run() {
                Statement statement = null;
                try {
                    statement = getConnection().createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM " + player_data_table_name + " WHERE PlayerUUID = '" + uuid.toString() + "';");

                    if (resultSet.next()) {
                        currency = resultSet.getDouble("Currency");
                        guildPrestigeLevel = resultSet.getInt("GuildPrestigeLevel");
                        maxGuildLevel = resultSet.getInt("GuildMaxLevel");
                        activeGuildLevel = resultSet.getInt("GuildActiveLevel");
                        score = resultSet.getInt("Score");
                        try {
                            questStatus = (PlayerQuests) resultSet.getBlob("QuestStatus");
                        } catch (Exception exception) {
                            //for players with no quest status
                            questStatus = new PlayerQuests(Bukkit.getPlayer(uuid));
                        }
                        kills = resultSet.getInt("Kills");
                        highestLevelKilled = resultSet.getInt("HighestLevelKilled");
                        deaths = resultSet.getInt("Deaths");
                        questsCompleted = resultSet.getInt("QuestsCompleted");

                        playerDataHashMap.put(uuid, playerData);

                        resultSet.close();
                        statement.close();
                        return;
                    } else {
                        currency = 0;
                        guildPrestigeLevel = 0;
                        maxGuildLevel = 1;
                        activeGuildLevel = 1;
                        questStatus = null;
                        score = 0;
                        kills = 0;
                        highestLevelKilled = 0;
                        deaths = 0;
                        questsCompleted = 0;

                        playerDataHashMap.put(uuid, playerData);

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

                        statement.close();
                        return;
                    }
                } catch (Exception e) {
                    if (statement != null) {
                        try {
                            statement.close();
                        } catch (SQLException throwables) {
                            new WarningMessage("Failed to close statement after failing player data creation!");
                            throwables.printStackTrace();
                        }
                    }
                    new WarningMessage("Something went wrong while generating a new player entry. This is bad! Tell the dev.");
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }

                new WarningMessage("No player entry detected, generating new entry!");

            }
        }.runTaskAsynchronously(MetadataHandler.PLUGIN);
    }

    private static Connection connection = null;
    public static final String database_name = "player_data.db";
    public static final String player_data_table_name = "PlayerData";

    public static Connection getConnection() throws Exception {
        File dataFolder = new File(MetadataHandler.PLUGIN.getDataFolder(), "data/" + database_name);
        if (connection == null || connection.isClosed()) {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            connection.setAutoCommit(true);
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
            new InfoMessage("Opened database successfully");

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

            for (Player player : Bukkit.getOnlinePlayers())
                new PlayerData(player.getUniqueId());

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            new WarningMessage("Failed to establish a connection to the SQLite database. This is not good.");
        }

        new PortOldData();

    }

    public static class PlayerDataEvents implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerLogin(PlayerJoinEvent event) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    new PlayerData(event.getPlayer().getUniqueId());
                }
            }.runTaskLaterAsynchronously(MetadataHandler.PLUGIN, 20);
        }

        @EventHandler
        public void onPlayerLogout(PlayerQuitEvent event) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    clearPlayerData(event.getPlayer().getUniqueId());
                    setDisplayName(event.getPlayer().getUniqueId(), event.getPlayer().getName());
                }
            }.runTaskLaterAsynchronously(MetadataHandler.PLUGIN, 20);
        }
    }

}
