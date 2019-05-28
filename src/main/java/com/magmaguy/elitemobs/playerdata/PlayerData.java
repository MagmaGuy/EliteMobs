package com.magmaguy.elitemobs.playerdata;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class PlayerData {

    public static HashMap<UUID, String> playerDisplayName = new HashMap<>();
    public static HashMap<UUID, Double> playerCurrency = new HashMap<>();
    public static HashMap<UUID, Integer> playerMaxGuildRank = new HashMap<>();
    public static HashMap<UUID, Integer> playerSelectedGuildRank = new HashMap<>();

    public static void initializePlayerData() {

        initializeDisplayName();
        initializeCurrency();
        initializeSelectedGuildRank();
        initializeMaxGuildRank();

    }

    public static void clearPlayerData() {

        playerDisplayName.clear();
        playerCurrency.clear();
        playerMaxGuildRank.clear();
        playerSelectedGuildRank.clear();

    }

    public static int databaseSyncTaskID = 0;
    public static boolean playerCacheChanged = false;
    public static boolean playerCurrencyChanged = false;
    public static boolean playerSelectedGuildRankChanged = false;
    public static boolean playerMaxGuildRankChanged = false;

    public static void synchronizeDatabases() {

        databaseSyncTaskID = new BukkitRunnable() {

            @Override
            public void run() {

                saveDatabases();

            }

        }.runTaskTimerAsynchronously(MetadataHandler.PLUGIN, 20 * 60, 20 * 60).getTaskId();

    }

    public static void saveDatabases() {

        if (playerCacheChanged)
            saveDisplayName();
        if (playerCurrencyChanged)
            saveCurrency();
        if (playerSelectedGuildRankChanged)
            saveSelectedGuildRank();
        if (playerMaxGuildRankChanged)
            saveMaxGuildRank();

    }

    private static void initializeDisplayName() {

        PlayerCacheData playerCacheData = new PlayerCacheData();

        for (String string : playerCacheData.configuration.getKeys(true)) {

            playerDisplayName.put(UUID.fromString(string), ConfigValues.playerCacheConfig.getString(string));

        }

    }

    private static void saveDisplayName() {

        PlayerCacheData playerCacheData = new PlayerCacheData();

        for (UUID uuid : playerDisplayName.keySet()) {

            playerCacheData.configuration.set(uuid.toString(), playerDisplayName.get(uuid));

        }

        playerCacheData.customConfigLoader.saveCustomConfig(PlayerCacheData.CONFIG_NAME, true);

        playerCacheChanged = false;

    }

    private static void initializeCurrency() {

        for (String string : PlayerMoneyData.getFileConfiguration().getKeys(true))
            playerCurrency.put(UUID.fromString(string), PlayerMoneyData.getDouble(string));

    }

    private static void saveCurrency() {

        for (UUID uuid : playerCurrency.keySet())
            PlayerMoneyData.getFileConfiguration().set(uuid.toString(), playerCurrency.get(uuid));

        PlayerMoneyData.saveConfig();
        playerCurrencyChanged = false;
    }

    private static void initializeMaxGuildRank() {

        PlayerMaxGuildRank playerMaxGuildRankClass = new PlayerMaxGuildRank();

        for (String string : playerMaxGuildRankClass.configuration.getKeys(true)) {

            playerMaxGuildRank.put(UUID.fromString(string), playerMaxGuildRankClass.configuration.getInt(string));

        }

    }

    private static void saveMaxGuildRank() {

        PlayerMaxGuildRank playerMaxGuildRankClass = new PlayerMaxGuildRank();

        for (UUID uuid : playerMaxGuildRank.keySet()) {

            playerMaxGuildRankClass.configuration.set(uuid.toString(), playerMaxGuildRank.get(uuid));

        }

        playerMaxGuildRankClass.customConfigLoader.saveCustomConfig(PlayerMaxGuildRank.CONFIG_NAME, true);

        playerMaxGuildRankChanged = false;

    }

    private static void initializeSelectedGuildRank() {
        for (String string : GuildRankData.getFileConfiguration().getKeys(true))
            playerSelectedGuildRank.put(UUID.fromString(string), GuildRankData.getInt(string));
    }

    private static void saveSelectedGuildRank() {
        for (UUID uuid : playerSelectedGuildRank.keySet())
            GuildRankData.getFileConfiguration().set(uuid.toString(), playerSelectedGuildRank.get(uuid));

        GuildRankData.saveConfig();
        playerSelectedGuildRankChanged = false;
    }

}
