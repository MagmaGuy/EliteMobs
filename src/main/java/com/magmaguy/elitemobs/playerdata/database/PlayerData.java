package com.magmaguy.elitemobs.playerdata.database;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DatabaseConfig;
import com.magmaguy.elitemobs.dungeons.DungeonBossLockout;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.playercooldowns.PlayerQuestCooldowns;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.ObjectSerializer;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
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
    @Getter
    private static final HashMap<UUID, PlayerData> playerDataHashMap = new HashMap<>();
    private static Connection connection = null;
    @Getter
    @Setter
    private double currency;
    @Getter
    @Setter
    private int score;
    @Getter
    @Setter
    private int kills;
    @Getter
    @Setter
    private int highestLevelKilled;
    @Getter
    @Setter
    private int deaths;
    @Getter
    @Setter
    private int questsCompleted;
    @Getter
    @Setter
    private List<Quest> quests = new ArrayList<>();
    @Getter
    @Setter
    private PlayerQuestCooldowns playerQuestCooldowns = null;
    @Getter
    @Setter
    private Location backTeleportLocation;
    @Getter
    @Setter
    private boolean useBookMenus;
    @Getter
    @Setter
    private boolean dismissEMStatusScreenMessage;
    @Getter
    @Setter
    private MatchInstance matchInstance = null;
    @Getter
    @Setter
    private DungeonBossLockout dungeonBossLockout = null;

    // Skill XP fields - each skill has independent progression
    @Getter
    @Setter
    private long skillXP_ARMOR = 0;
    @Getter
    @Setter
    private long skillXP_SWORDS = 0;
    @Getter
    @Setter
    private long skillXP_AXES = 0;
    @Getter
    @Setter
    private long skillXP_BOWS = 0;
    @Getter
    @Setter
    private long skillXP_CROSSBOWS = 0;
    @Getter
    @Setter
    private long skillXP_TRIDENTS = 0;
    @Getter
    @Setter
    private long skillXP_HOES = 0;
    @Getter
    @Setter
    private long skillXP_MACES = 0;
    @Getter
    @Setter
    private long skillXP_SPEARS = 0;

    // Skill bonus selections - JSON string mapping skill types to selected skill IDs
    @Getter
    @Setter
    private String skillBonusSelections = "{}";

    // Gambling debt - tracks how much the player owes from gambling
    @Getter
    @Setter
    private double gamblingDebt = 0;

    public PlayerData(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            Logger.warn("EliteMobs did not initialize player data for uuid " + uuid + " because Minecraft does not recognize this as a valid player!");
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
                    if (resultSet.next()) {
                        readExistingData(statement, uuid, resultSet);
                    } else {
                        writeNewData(statement, uuid);
                    }
                    resultSet.close();
                    statement.close();
                } catch (Exception e) {
                    if (statement != null) {
                        try {
                            statement.close();
                        } catch (SQLException throwables) {
                            Logger.warn("Failed to close statement after failing player data creation!");
                            throwables.printStackTrace();
                        }
                    }
                    Logger.warn("Something went wrong while generating a new player entry. This is bad! Tell the dev.");
                    Logger.warn(e.getClass().getName() + ": " + e.getMessage());
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
            Logger.warn("Failed to serialize player quest data!");
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
                return 0;
        return playerDataHashMap.get(uuid).currency;
    }

    public static void setCurrency(UUID uuid, double currency) {
        setDatabaseValue(uuid, "Currency", currency);
        if (playerDataHashMap.containsKey(uuid))
            playerDataHashMap.get(uuid).currency = currency;
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
            Logger.warn("Failed to convert quest ID from command into a valid UUID format!");
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
            Logger.warn("Failed to register player quest cooldowns!");
            ex.printStackTrace();
        }
    }

    @Nullable
    public static DungeonBossLockout getDungeonBossLockout(UUID uuid) {
        try {
            if (!isInMemory(uuid))
                return (DungeonBossLockout) ObjectSerializer.fromString((String) getDatabaseBlob(uuid, "DungeonBossLockouts"));
            if (playerDataHashMap.get(uuid) == null) return new DungeonBossLockout();
            return playerDataHashMap.get(uuid).dungeonBossLockout == null ? new DungeonBossLockout() : playerDataHashMap.get(uuid).dungeonBossLockout;
        } catch (Exception ex) {
            return new DungeonBossLockout();
        }
    }

    public static void updateDungeonBossLockout(UUID uuid, DungeonBossLockout dungeonBossLockout) {
        try {
            // Clean up expired lockouts before saving
            dungeonBossLockout.cleanupExpiredLockouts();
            setDatabaseValue(uuid, "DungeonBossLockouts", ObjectSerializer.toString(dungeonBossLockout));
            if (playerDataHashMap.containsKey(uuid))
                playerDataHashMap.get(uuid).dungeonBossLockout = dungeonBossLockout;
        } catch (Exception ex) {
            Logger.warn("Failed to update dungeon boss lockouts!");
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
                    if (value instanceof String) {
                        sql = "UPDATE " + PLAYER_DATA_TABLE_NAME + " SET " + key + " = '" + value + "' WHERE PlayerUUID = '" + uuid.toString() + "';";
                    } else if (value instanceof Boolean) {
                        sql = "UPDATE " + PLAYER_DATA_TABLE_NAME + " SET " + key + " = " + (((Boolean) value) ? 1 : 0) + " WHERE PlayerUUID = '" + uuid.toString() + "';";
                    } else {
                        sql = "UPDATE " + PLAYER_DATA_TABLE_NAME + " SET " + key + " = " + value + " WHERE PlayerUUID = '" + uuid.toString() + "';";
                    }
                    statement.executeUpdate(sql);
                    statement.close();
                } catch (Exception e) {
                    Logger.warn("Failed to update database value.");
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
            Logger.warn("Failed to get blob value from database!");
            Logger.warn("UUID: " + uuid + " | Value: " + value);
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

    // ==================== SKILL XP METHODS ====================

    /**
     * Gets the total XP for a specific skill.
     *
     * @param uuid      The player's UUID
     * @param skillType The skill type to check
     * @return The total XP for that skill
     */
    public static long getSkillXP(UUID uuid, SkillType skillType) {
        String columnName = skillType.getColumnName();
        if (!isInMemory(uuid))
            return getDatabaseLong(uuid, columnName);
        return getSkillXPByType(playerDataHashMap.get(uuid), skillType);
    }

    /**
     * Sets the total XP for a specific skill.
     *
     * @param uuid      The player's UUID
     * @param skillType The skill type to set
     * @param xp        The new total XP value
     */
    public static void setSkillXP(UUID uuid, SkillType skillType, long xp) {
        String columnName = skillType.getColumnName();
        setDatabaseValue(uuid, columnName, xp);
        if (playerDataHashMap.containsKey(uuid))
            setSkillXPByType(playerDataHashMap.get(uuid), skillType, xp);
    }

    /**
     * Adds XP to a specific skill.
     *
     * @param uuid      The player's UUID
     * @param skillType The skill type to add XP to
     * @param xpToAdd   The amount of XP to add
     * @return The new total XP for that skill
     */
    public static long addSkillXP(UUID uuid, SkillType skillType, long xpToAdd) {
        long currentXP = getSkillXP(uuid, skillType);
        long newXP = currentXP + xpToAdd;
        setSkillXP(uuid, skillType, newXP);
        return newXP;
    }

    /**
     * Gets the XP for all skills as an array.
     *
     * @param uuid The player's UUID
     * @return Array of XP values indexed by SkillType ordinal
     */
    public static long[] getAllSkillXP(UUID uuid) {
        long[] xpArray = new long[SkillType.values().length];
        for (SkillType skillType : SkillType.values()) {
            xpArray[skillType.ordinal()] = getSkillXP(uuid, skillType);
        }
        return xpArray;
    }

    /**
     * Gets the player's overall combat level.
     * This replaces the old guild rank system.
     *
     * @param uuid The player's UUID
     * @return The player's combat level (average of top 2 weapons + armor)
     */
    public static int getPlayerLevel(UUID uuid) {
        return com.magmaguy.elitemobs.skills.CombatLevelCalculator.calculateCombatLevel(uuid);
    }

    /**
     * Gets the player's skill level for a specific skill type.
     *
     * @param uuid The player's UUID
     * @param skillType The skill type to check
     * @return The skill level (1-100+)
     */
    public static int getSkillLevel(UUID uuid, SkillType skillType) {
        long xp = getSkillXP(uuid, skillType);
        return com.magmaguy.elitemobs.skills.SkillXPCalculator.levelFromTotalXP(xp);
    }

    /**
     * Helper method to get skill XP from a PlayerData instance by SkillType.
     */
    private static long getSkillXPByType(PlayerData playerData, SkillType skillType) {
        return switch (skillType) {
            case ARMOR -> playerData.skillXP_ARMOR;
            case SWORDS -> playerData.skillXP_SWORDS;
            case AXES -> playerData.skillXP_AXES;
            case BOWS -> playerData.skillXP_BOWS;
            case CROSSBOWS -> playerData.skillXP_CROSSBOWS;
            case TRIDENTS -> playerData.skillXP_TRIDENTS;
            case HOES -> playerData.skillXP_HOES;
            case MACES -> playerData.skillXP_MACES;
            case SPEARS -> playerData.skillXP_SPEARS;
        };
    }

    /**
     * Helper method to set skill XP on a PlayerData instance by SkillType.
     */
    private static void setSkillXPByType(PlayerData playerData, SkillType skillType, long xp) {
        switch (skillType) {
            case ARMOR -> playerData.skillXP_ARMOR = xp;
            case SWORDS -> playerData.skillXP_SWORDS = xp;
            case AXES -> playerData.skillXP_AXES = xp;
            case BOWS -> playerData.skillXP_BOWS = xp;
            case CROSSBOWS -> playerData.skillXP_CROSSBOWS = xp;
            case TRIDENTS -> playerData.skillXP_TRIDENTS = xp;
            case HOES -> playerData.skillXP_HOES = xp;
            case MACES -> playerData.skillXP_MACES = xp;
            case SPEARS -> playerData.skillXP_SPEARS = xp;
        }
    }

    // ==================== SKILL BONUS SELECTION METHODS ====================

    /**
     * Gets the skill bonus selections JSON string for a player.
     *
     * @param uuid The player's UUID
     * @return JSON string of skill bonus selections (or "{}" if none)
     */
    public static String getSkillBonusSelections(UUID uuid) {
        if (!isInMemory(uuid)) {
            String dbValue = getDatabaseString(uuid, "SkillBonusSelections");
            return dbValue != null ? dbValue : "{}";
        }
        PlayerData data = playerDataHashMap.get(uuid);
        return data != null ? data.skillBonusSelections : "{}";
    }

    /**
     * Sets the skill bonus selections JSON string for a player.
     *
     * @param uuid       The player's UUID
     * @param selections JSON string of skill bonus selections
     */
    public static void setSkillBonusSelections(UUID uuid, String selections) {
        setDatabaseValue(uuid, "SkillBonusSelections", selections);
        if (playerDataHashMap.containsKey(uuid))
            playerDataHashMap.get(uuid).skillBonusSelections = selections;
    }

    // ==================== GAMBLING DEBT METHODS ====================

    /**
     * Gets the gambling debt for a player.
     *
     * @param uuid The player's UUID
     * @return The amount of debt the player owes
     */
    public static double getGamblingDebt(UUID uuid) {
        if (!isInMemory(uuid))
            return getDatabaseDouble(uuid, "GamblingDebt");
        PlayerData data = playerDataHashMap.get(uuid);
        return data != null ? data.gamblingDebt : 0;
    }

    /**
     * Sets the gambling debt for a player.
     * Debt is clamped between 0 and the maximum allowed debt.
     *
     * @param uuid The player's UUID
     * @param debt The new debt amount
     */
    public static void setGamblingDebt(UUID uuid, double debt) {
        // Clamp debt to valid range (0 to max debt)
        debt = Math.max(0, Math.min(500, debt));
        setDatabaseValue(uuid, "GamblingDebt", debt);
        if (playerDataHashMap.containsKey(uuid))
            playerDataHashMap.get(uuid).gamblingDebt = debt;
    }

    /**
     * Adds to the player's gambling debt.
     *
     * @param uuid   The player's UUID
     * @param amount The amount of debt to add
     */
    public static void addGamblingDebt(UUID uuid, double amount) {
        double currentDebt = getGamblingDebt(uuid);
        setGamblingDebt(uuid, currentDebt + amount);
    }

    /**
     * Reduces the player's gambling debt.
     *
     * @param uuid   The player's UUID
     * @param amount The amount of debt to reduce
     */
    public static void reduceGamblingDebt(UUID uuid, double amount) {
        double currentDebt = getGamblingDebt(uuid);
        setGamblingDebt(uuid, currentDebt - amount);
    }

    /**
     * Checks if the player is in gambling debt.
     *
     * @param uuid The player's UUID
     * @return true if the player has gambling debt
     */
    public static boolean hasGamblingDebt(UUID uuid) {
        return getGamblingDebt(uuid) > 0;
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
            Logger.warn("Failed to get boolean value from database!");
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
            Logger.warn("Failed to get string value from database!");
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
            Logger.warn("Failed to get double value from database!");
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
            Logger.warn("Failed to get integer value from database!");
            e.printStackTrace();
            return null;
        }
    }

    private static Long getDatabaseLong(UUID uuid, String value) {
        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + PLAYER_DATA_TABLE_NAME + " WHERE PlayerUUID = '" + uuid.toString() + "';");
            long reply = resultSet.getLong(value);
            resultSet.close();
            statement.close();
            return reply;
        } catch (Exception e) {
            Logger.warn("Failed to get long value from database!");
            e.printStackTrace();
            return 0L;
        }
    }

    public static Connection getConnection() throws Exception {
        File dataFolder = new File(MetadataHandler.PLUGIN.getDataFolder(), "data/" + DATABASE_NAME);
        if (connection == null || connection.isClosed()) {
            if (!DatabaseConfig.isUseMySQL()) {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
                connection.setAutoCommit(true);
            } else {
                Class.forName("com.mysql.jdbc.Driver");
                String URL = "jdbc:mysql://" + DatabaseConfig.getMysqlHost() + ":"
                        + DatabaseConfig.getMysqlPort() + "/" + DatabaseConfig.mysqlDatabaseName
                        + "?useSSL=" + DatabaseConfig.useSSL
                        + "&createDatabaseIfNotExist=true";
                String USER = DatabaseConfig.getMysqlUsername();
                String PASS = DatabaseConfig.getMysqlPassword();
                connection = DriverManager.getConnection(URL, USER, PASS);
                connection.setAutoCommit(true);
            }
        }
        return connection;
    }

    public static void initializeDatabaseConnection() {
        new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/data").mkdirs();
        try {
            Logger.info("Opened database successfully");
            GenerateDatabase.generate();
            for (Player player : Bukkit.getOnlinePlayers())
                new PlayerData(player.getUniqueId());
        } catch (Exception e) {
            Logger.warn(e.getClass().getName() + ": " + e.getMessage());
            Logger.warn("Failed to establish a connection to the SQLite database. Player data will not be saved! Is your MySQL configuration valid and is your MySQL server running?");
            e.printStackTrace();
        }

        new PortOldData();
    }

    public static void closeConnection() {
        playerDataHashMap.clear();
        try {
            if (connection == null) return;
            connection.close();
        } catch (Exception ex) {
            Logger.warn("Could not correctly close database connection.");
        }
    }

    private void readExistingData(Statement statement, UUID uuid, ResultSet resultSet) throws Exception {
        playerDataHashMap.put(uuid, this);
        currency = resultSet.getDouble("Currency");
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
                Logger.warn("Failed to serialize quest data for player " + Bukkit.getPlayer(uuid) + " ! This player's quest data will be wiped to prevent future errors.");
                try {
                    resetQuests(uuid);
                } catch (Exception ex2) {
                    Logger.warn("Failed to reset quest data! Ironic.");
                    ex2.printStackTrace();
                }
            }
        }

        if (resultSet.getBytes("PlayerQuestCooldowns") != null) {
            try {
                playerQuestCooldowns = (PlayerQuestCooldowns) ObjectSerializer.fromString(new String(resultSet.getBytes("PlayerQuestCooldowns"), StandardCharsets.UTF_8));
                playerQuestCooldowns.startCooldowns(uuid);
            } catch (Exception exception) {
                Logger.warn("Failed to get player quest cooldowns!  ! This player's quest cooldowns will be wiped to prevent future errors.");
                try {
                    resetPlayerQuestCooldowns(uuid);
                } catch (Exception ex2) {
                    Logger.warn("Failed to reset quest cooldowns! Ironic.");
                    ex2.printStackTrace();
                }
            }
        }

        if (resultSet.getObject("UseBookMenus") != null) {
            useBookMenus = resultSet.getBoolean("UseBookMenus");
        } else {
            setUseBookMenus(Bukkit.getPlayer(uuid), true);
        }

        if (resultSet.getObject("DismissEMStatusScreenMessage") != null) {
            dismissEMStatusScreenMessage = resultSet.getBoolean("DismissEMStatusScreenMessage");
        } else {
            setDismissEMStatusScreenMessage(Bukkit.getPlayer(uuid), false);
        }

        if (resultSet.getBytes("DungeonBossLockouts") != null) {
            try {
                dungeonBossLockout = (DungeonBossLockout) ObjectSerializer.fromString(new String(resultSet.getBytes("DungeonBossLockouts"), StandardCharsets.UTF_8));
                // Clean up expired lockouts on load
                dungeonBossLockout.cleanupExpiredLockouts();
            } catch (Exception exception) {
                Logger.warn("Failed to get dungeon boss lockouts! This player's lockouts will be reset.");
                dungeonBossLockout = new DungeonBossLockout();
            }
        } else {
            dungeonBossLockout = new DungeonBossLockout();
        }

        // Read skill XP values
        skillXP_ARMOR = resultSet.getLong("SkillXP_ARMOR");
        skillXP_SWORDS = resultSet.getLong("SkillXP_SWORDS");
        skillXP_AXES = resultSet.getLong("SkillXP_AXES");
        skillXP_BOWS = resultSet.getLong("SkillXP_BOWS");
        skillXP_CROSSBOWS = resultSet.getLong("SkillXP_CROSSBOWS");
        skillXP_TRIDENTS = resultSet.getLong("SkillXP_TRIDENTS");
        skillXP_HOES = resultSet.getLong("SkillXP_HOES");
        try {
            skillXP_MACES = resultSet.getLong("SkillXP_MACES");
            skillXP_SPEARS = resultSet.getLong("SkillXP_SPEARS");
        } catch (SQLException e) {
            // Columns may not exist yet for older databases - will be added on next save
            skillXP_MACES = 0;
            skillXP_SPEARS = 0;
        }

        // Read skill bonus selections
        String skillSelections = resultSet.getString("SkillBonusSelections");
        skillBonusSelections = (skillSelections != null) ? skillSelections : "{}";

        // Read gambling debt
        gamblingDebt = resultSet.getDouble("GamblingDebt");

        Logger.info("User " + uuid + " data successfully read!");
    }

    private void writeNewData(Statement statement, UUID uuid) throws Exception {
        playerDataHashMap.put(uuid, this);
        currency = 0;
        score = 0;
        kills = 0;
        highestLevelKilled = 0;
        deaths = 0;
        questsCompleted = 0;
        // Initialize all skill XP to 0
        skillXP_ARMOR = 0;
        skillXP_SWORDS = 0;
        skillXP_AXES = 0;
        skillXP_BOWS = 0;
        skillXP_CROSSBOWS = 0;
        skillXP_TRIDENTS = 0;
        skillXP_HOES = 0;
        skillXP_MACES = 0;
        skillXP_SPEARS = 0;
        // Initialize skill bonus selections to empty
        skillBonusSelections = "{}";
        // Initialize gambling debt to 0
        gamblingDebt = 0;
        statement = getConnection().createStatement();
        String sql = "INSERT INTO " + PLAYER_DATA_TABLE_NAME + " (" +
                "PlayerUUID," +
                " DisplayName," +
                " Currency," +
                " Score," +
                " Kills," +
                " HighestLevelKilled," +
                " Deaths," +
                " QuestsCompleted," +
                " SkillXP_ARMOR," +
                " SkillXP_SWORDS," +
                " SkillXP_AXES," +
                " SkillXP_BOWS," +
                " SkillXP_CROSSBOWS," +
                " SkillXP_TRIDENTS," +
                " SkillXP_HOES," +
                " SkillXP_MACES," +
                " SkillXP_SPEARS," +
                " SkillBonusSelections," +
                " GamblingDebt) " +
                //identifier
                "VALUES ('" + uuid + "'," +
                //display name
                " '" + Bukkit.getPlayer(uuid).getName() + "'," +
                //currency
                " 0," +
                //score
                "0," +
                //kills
                "0," +
                //highestLevelKilled
                "0," +
                //deaths
                "0," +
                //questsCompleted
                "0," +
                //skill XP values (all start at 0)
                "0,0,0,0,0,0,0,0,0," +
                //skill bonus selections (empty JSON)
                "'{}', " +
                //gambling debt (starts at 0)
                "0);";
        statement.executeUpdate(sql);
        statement.close();
        Logger.info("No player entry detected, generating new entry!");
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
