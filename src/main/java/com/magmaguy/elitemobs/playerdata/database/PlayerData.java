package com.magmaguy.elitemobs.playerdata.database;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.playercooldowns.PlayerQuestCooldowns;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.ObjectSerializer;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerData {

    @Getter
    private static final String DATABASE_NAME = "player_data.db";
    @Getter
    private static final String PLAYER_DATA_TABLE_NAME = "PlayerData";
    private static final HashMap<UUID, PlayerData> playerDataHashMap = new HashMap<>();
    private static Connection connection = null;
    private double currency;
    private int guildPrestigeLevel;
    private int maxGuildLevel;
    private int activeGuildLevel;
    private int score;
    private int kills;
    private int highestLevelKilled;
    private int deaths;
    private int questsCompleted;
    private List<Quest> quests = new ArrayList<>();
    private PlayerQuestCooldowns playerQuestCooldowns = null;
    private Location backTeleportLocation;
    private boolean useBookMenus;
    private boolean dismissEMStatusScreenMessage;
    //This is currently not stored in the database, time will tell if it is necessary to do so
    private MatchInstance matchInstance = null;

    /**
     * Called when a player logs in, storing their data in memory
     *
     * @param uuid
     */
    public PlayerData(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            new WarningMessage("EliteMobs did not initialize player data for uuid " + uuid + " because Minecraft does not recognize this as a valid player!");
            return;
        }
        PermissionAttachment permissionAttachment = player.addAttachment(MetadataHandler.PLUGIN);
        permissionAttachment.setPermission("elitequest.*", false);
        new BukkitRunnable() {
            @Override
            public void run() {
                Statement statement = null;
                try {
                    statement = getConnection().createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM " + PLAYER_DATA_TABLE_NAME + " WHERE PlayerUUID = '" + uuid + "';");
                    //case for there being data to read
                    if (resultSet.next()) {
                        readExistingData(statement, uuid, resultSet);
                    } else {
                        //case for new data to be written
                        writeNewData(statement, uuid);
                    }
                    resultSet.close();
                    statement.close();

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
                    new WarningMessage(e.getClass().getName() + ": " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(MetadataHandler.PLUGIN);
    }

    public static PlayerData getPlayerData(UUID player) {
        return playerDataHashMap.get(player);
    }

    public static void updateQuestStatus(UUID uuid) {
        List<Quest> playerQuests = getQuests(uuid);
        try {
            setDatabaseValue(uuid, "QuestStatus", ObjectSerializer.toString((ArrayList) playerQuests));
            if (playerDataHashMap.containsKey(uuid))
                playerDataHashMap.get(uuid).quests = playerQuests;
        } catch (Exception ex) {
            new WarningMessage("Failed to serialize player quest data!");
            ex.printStackTrace();
        }
    }

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

    public static List<Quest> getQuests(UUID uuid) {
        try {
            if (!isInMemory(uuid))
                return (List<Quest>) ObjectSerializer.fromString((String) getDatabaseBlob(uuid, "QuestStatus"));
            if (playerDataHashMap.get(uuid) == null) return new ArrayList<>();
            return playerDataHashMap.get(uuid).quests == null ? new ArrayList<>() : playerDataHashMap.get(uuid).quests;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public static Quest getQuest(UUID uuid, String questID) {
        try {
            UUID questUUID = UUID.fromString(questID);
            return getQuest(uuid, questUUID);
        } catch (Exception ex) {
            new WarningMessage("Failed to convert quest ID from command into a valid UUID format!");
            return null;
        }
    }

    public static Quest getQuest(UUID uuid, UUID questUUID) {
        List<Quest> questList = null;
        try {
            if (!isInMemory(uuid))
                questList = (List<Quest>) ObjectSerializer.fromString((String) getDatabaseBlob(uuid, "QuestStatus"));
            else
                questList = playerDataHashMap.get(uuid).quests;
        } catch (Exception ex) {
            return null;
        }
        for (Quest iteratedQuest : questList)
            if (iteratedQuest.getQuestID().equals(questUUID))
                return iteratedQuest;
        return null;
    }

    public static void resetQuests(UUID uuid) {
        getQuests(uuid).clear();
        updateQuestStatus(uuid);
    }

    public static void removeQuest(UUID uuid, Quest quest) {
        if (quest == null) return;
        playerDataHashMap.get(uuid).quests.removeIf(iteratedQuest -> iteratedQuest.getQuestID().equals(quest.getQuestID()));
        updateQuestStatus(uuid);
    }

    public static void addQuest(UUID uuid, Quest quest) {
        playerDataHashMap.get(uuid).quests.add(quest);
        updateQuestStatus(uuid);
    }

    public static void updateQuestStatus(UUID uuid, Quest quest) {
        //this might be removed in the future
        updateQuestStatus(uuid);
    }

    @Nullable
    public static PlayerQuestCooldowns getPlayerQuestCooldowns(UUID uuid) {
        try {
            if (!isInMemory(uuid))
                return (PlayerQuestCooldowns) ObjectSerializer.fromString((String) getDatabaseBlob(uuid, "PlayerQuestCooldowns"));
            if (playerDataHashMap.get(uuid) == null) return PlayerQuestCooldowns.initializePlayer();
            return playerDataHashMap.get(uuid).playerQuestCooldowns == null ? PlayerQuestCooldowns.initializePlayer() : playerDataHashMap.get(uuid).playerQuestCooldowns;
        } catch (Exception ex) {
            return null;
        }
    }

    public static void resetPlayerQuestCooldowns(UUID uuid) {
        updatePlayerQuestCooldowns(uuid, PlayerQuestCooldowns.initializePlayer());
    }

    public static void updatePlayerQuestCooldowns(UUID uuid, PlayerQuestCooldowns playerQuestCooldowns) {
        try {
            setDatabaseValue(uuid, "PlayerQuestCooldowns", ObjectSerializer.toString(playerQuestCooldowns));
        } catch (Exception ex) {
            new WarningMessage("Failed to register player quest cooldowns!");
            ex.printStackTrace();
        }
    }

    public static void setDatabaseValue(UUID uuid, String key, Object value) {

        new BukkitRunnable() {
            @Override
            public void run() {
                Statement statement = null;
                try {
                    statement = getConnection().createStatement();
                    String sql;
                    if (value instanceof String)
                        sql = "UPDATE " + PLAYER_DATA_TABLE_NAME + " SET " + key + " = '" + value + "' WHERE PlayerUUID = '" + uuid.toString() + "';";
                    else
                        sql = "UPDATE " + PLAYER_DATA_TABLE_NAME + " SET " + key + " = " + value + " WHERE PlayerUUID = '" + uuid.toString() + "';";
                    statement.executeUpdate(sql);
                    statement.close();

                } catch (Exception e) {
                    new WarningMessage("Failed to update database value.");
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(MetadataHandler.PLUGIN);

    }

    private static Object getDatabaseBlob(UUID uuid, String value) {
        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + PLAYER_DATA_TABLE_NAME + " WHERE PlayerUUID = '" + uuid.toString() + "';");
            byte[] bytes = resultSet.getBytes(value);
            resultSet.close();
            statement.close();
            if (bytes == null) return null;
            return new String(bytes);
        } catch (Exception e) {
            new WarningMessage("Failed to get blob value from database!");
            new WarningMessage("UUID: " + uuid + " | Value: " + value);
            e.printStackTrace();
            return null;
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

    public static Location getBackTeleportLocation(Player player) {
        if (!isInMemory(player.getUniqueId()))
            return ConfigurationLocation.serialize(getDatabaseString(player.getUniqueId(), "BackTeleportLocation"));

        return playerDataHashMap.get(player.getUniqueId()).backTeleportLocation;
    }

    public static void setBackTeleportLocation(Player player, Location location) {
        setDatabaseValue(player.getUniqueId(), "BackTeleportLocation", ConfigurationLocation.deserialize(location));
        playerDataHashMap.get(player.getUniqueId()).backTeleportLocation = location;
    }

    public static boolean getUseBookMenus(UUID uuid) {
        if (!isInMemory(uuid))
            return getDatabaseBoolean(uuid, "UseBookMenus");
        return playerDataHashMap.get(uuid).useBookMenus;
    }

    public static void setUseBookMenus(Player player, boolean use) {
        setDatabaseValue(player.getUniqueId(), "UseBookMenus", use);
        playerDataHashMap.get(player.getUniqueId()).useBookMenus = use;
    }

    public static boolean getDismissEMStatusScreenMessage(UUID uuid) {
        if (!isInMemory(uuid))
            return getDatabaseBoolean(uuid, "DismissEMStatusScreenMessage");
        return playerDataHashMap.get(uuid).dismissEMStatusScreenMessage;
    }

    public static void setDismissEMStatusScreenMessage(Player player, boolean use) {
        setDatabaseValue(player.getUniqueId(), "DismissEMStatusScreenMessage", use);
        playerDataHashMap.get(player.getUniqueId()).dismissEMStatusScreenMessage = use;
    }

    public static MatchInstance getMatchInstance(Player player) {
        if (player == null || playerDataHashMap.get(player.getUniqueId()) == null) return null;
        return playerDataHashMap.get(player.getUniqueId()).matchInstance;
    }

    public static void setMatchInstance(Player player, MatchInstance newMatchInstance) {
        if (playerDataHashMap.get(player.getUniqueId()) != null)
            playerDataHashMap.get(player.getUniqueId()).matchInstance = newMatchInstance;
    }

    private static Boolean getDatabaseBoolean(UUID uuid, String value) {
        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + PLAYER_DATA_TABLE_NAME + " WHERE PlayerUUID = '" + uuid.toString() + "';");
            boolean reply = resultSet.getBoolean(value);
            resultSet.close();
            statement.close();
            return reply;
        } catch (Exception e) {
            new WarningMessage("Failed to get double value from database!");
            e.printStackTrace();
            return null;
        }
    }


    private static String getDatabaseString(UUID uuid, String value) {
        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + PLAYER_DATA_TABLE_NAME + " WHERE PlayerUUID = '" + uuid.toString() + "';");
            String reply = resultSet.getString(value);
            resultSet.close();
            statement.close();
            return reply;
        } catch (Exception e) {
            new WarningMessage("Failed to get string value from database!");
            e.printStackTrace();
            return null;
        }
    }

    private static Double getDatabaseDouble(UUID uuid, String value) {
        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + PLAYER_DATA_TABLE_NAME + " WHERE PlayerUUID = '" + uuid.toString() + "';");
            double reply = resultSet.getDouble(value);
            resultSet.close();
            statement.close();
            return reply;
        } catch (Exception e) {
            new WarningMessage("Failed to get double value from database!");
            e.printStackTrace();
            return null;
        }
    }

    private static Integer getDatabaseInteger(UUID uuid, String value) {
        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + PLAYER_DATA_TABLE_NAME + " WHERE PlayerUUID = '" + uuid.toString() + "';");
            int reply = resultSet.getInt(value);
            resultSet.close();
            statement.close();
            return reply;
        } catch (Exception e) {
            new WarningMessage("Failed to get integer value from database!");
            e.printStackTrace();
            return null;
        }
    }

    public static Connection getConnection() throws Exception {
        File dataFolder = new File(MetadataHandler.PLUGIN.getDataFolder(), "data/" + DATABASE_NAME);
        if (connection == null || connection.isClosed()) {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            connection.setAutoCommit(true);
        }
        return connection;
    }

    public static void initializeDatabaseConnection() {
        new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/data").mkdirs();
        try {
            new InfoMessage("Opened database successfully");
            GenerateDatabase.generate();
            for (Player player : Bukkit.getOnlinePlayers())
                new PlayerData(player.getUniqueId());
        } catch (Exception e) {
            new WarningMessage(e.getClass().getName() + ": " + e.getMessage());
            new WarningMessage("Failed to establish a connection to the SQLite database. This is not good.");
        }

        new PortOldData();
    }

    public static void closeConnection() {
        try {
            if (connection == null) return;
            connection.close();
        } catch (Exception ex) {
            new WarningMessage("Could not correctly close database connection.");
        }
    }

    private void readExistingData(Statement statement, UUID uuid, ResultSet resultSet) throws Exception {
        playerDataHashMap.put(uuid, this);
        currency = resultSet.getDouble("Currency");
        guildPrestigeLevel = resultSet.getInt("GuildPrestigeLevel");
        maxGuildLevel = resultSet.getInt("GuildMaxLevel");
        activeGuildLevel = resultSet.getInt("GuildActiveLevel");
        score = resultSet.getInt("Score");
        kills = resultSet.getInt("Kills");
        highestLevelKilled = resultSet.getInt("HighestLevelKilled");
        deaths = resultSet.getInt("Deaths");
        questsCompleted = resultSet.getInt("QuestsCompleted");
        backTeleportLocation = ConfigurationLocation.serialize(resultSet.getString("BackTeleportLocation"));

        if (resultSet.getBytes("QuestStatus") != null) {
            try {
                quests = (List<Quest>) ObjectSerializer.fromString(new String(resultSet.getBytes("QuestStatus"), StandardCharsets.UTF_8));
                //Serializes ItemStack which require specific handling, necessary recovering the rewards
                for (Quest quest : quests)
                    if (quest instanceof CustomQuest)
                        ((CustomQuest) quest).applyTemporaryPermissions(Bukkit.getPlayer(uuid));
            } catch (Exception ex) {
                new WarningMessage("Failed to serialize quest data for player " + Bukkit.getPlayer(uuid) + " ! This player's quest data will be wiped to prevent future errors.");
                try {
                    resetQuests(uuid);
                } catch (Exception ex2) {
                    new WarningMessage("Failed to reset quest data! Ironic.");
                    ex2.printStackTrace();
                }
            }
        }

        if (resultSet.getBytes("PlayerQuestCooldowns") != null) {
            try {
                playerQuestCooldowns = (PlayerQuestCooldowns) ObjectSerializer.fromString(new String(resultSet.getBytes("PlayerQuestCooldowns"), StandardCharsets.UTF_8));
                playerQuestCooldowns.startCooldowns(uuid);
            } catch (Exception exception) {
                new WarningMessage("Failed to get player quest cooldowns!  ! This player's quest cooldowns will be wiped to prevent future errors.");
                try {
                    resetPlayerQuestCooldowns(uuid);
                } catch (Exception ex2) {
                    new WarningMessage("Failed to reset quest cooldowns! Ironic.");
                    ex2.printStackTrace();
                }
            }
        }

        if (resultSet.getObject("UseBookMenus") != null) {
            useBookMenus = resultSet.getBoolean("UseBookMenus");

        } else {
            setUseBookMenus(Bukkit.getPlayer(uuid), false);
        }

        if (resultSet.getObject("DismissEMStatusScreenMessage") != null) {
            dismissEMStatusScreenMessage = resultSet.getBoolean("UseBookMenus");
        } else {
            setDismissEMStatusScreenMessage(Bukkit.getPlayer(uuid), false);
        }

        new InfoMessage("User " + uuid + " data successfully read!");
    }

    private void writeNewData(Statement statement, UUID uuid) throws Exception {
        playerDataHashMap.put(uuid, this);
        currency = 0;
        guildPrestigeLevel = 0;
        maxGuildLevel = 1;
        activeGuildLevel = 1;
        score = 0;
        kills = 0;
        highestLevelKilled = 0;
        deaths = 0;
        questsCompleted = 0;
        statement = getConnection().createStatement();
        String sql = "INSERT INTO " + PLAYER_DATA_TABLE_NAME +
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
                "VALUES ('" + uuid + "'," +
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

        new InfoMessage("No player entry detected, generating new entry!");
    }

    public static class PlayerDataEvents implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerLogin(PlayerJoinEvent event) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (Bukkit.getPlayer(event.getPlayer().getUniqueId()) == null) return;
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
