package com.magmaguy.elitemobs.playerdata;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.Statement;
import java.util.HashSet;
import java.util.UUID;

public class PortOldData {

    public PortOldData() {
        File playerCache = new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/data/playerCache.yml");
        File playerGuildRank = new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/data/playerGuildRank.yml");
        File playerMaxGuildRank = new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/data/playerMaxGuildRank.yml");
        File playerMoneyData = new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/data/playerMoneyData.yml");

        if (!playerCache.isFile() && !playerGuildRank.isFile() && !playerMaxGuildRank.isFile() && !playerMoneyData.isFile())
            return;

        FileConfiguration playerCacheConfig = null,
                playerGuildRankConfig = null,
                playerMaxGuildRankConfig = null,
                playerMoneyDataConfig = null;

        HashSet<UUID> uuids = new HashSet<>();

        if (playerCache.exists()) {
            playerCacheConfig = YamlConfiguration.loadConfiguration(playerCache);
            for (String string : playerCacheConfig.getKeys(false))
                uuids.add(UUID.fromString(string));
        }

        if (playerGuildRank.exists()) {
            playerGuildRankConfig = YamlConfiguration.loadConfiguration(playerGuildRank);
            for (String string : playerGuildRankConfig.getKeys(false))
                uuids.add(UUID.fromString(string));
        }

        if (playerMaxGuildRank.exists()) {
            playerMaxGuildRankConfig = YamlConfiguration.loadConfiguration(playerMaxGuildRank);
            for (String string : playerMaxGuildRankConfig.getKeys(false))
                uuids.add(UUID.fromString(string));
        }

        if (playerMoneyData.exists()) {
            playerMoneyDataConfig = YamlConfiguration.loadConfiguration(playerMoneyData);
            for (String string : playerMoneyDataConfig.getKeys(false))
                uuids.add(UUID.fromString(string));
        }

        if (uuids.isEmpty()) {
            deleteConfigs(playerCache, playerGuildRank, playerMaxGuildRank, playerMoneyData);
            return;
        }

        boolean errored = false;

        for (UUID uuid : uuids) {

            boolean wasOldMaxRank = false;

            String displayName = null;
            if (playerCacheConfig != null) {
                if (playerCacheConfig.contains(uuid.toString()))
                    displayName = playerCacheConfig.getString(uuid.toString());
            }
            if (displayName == null) {
                displayName = "PlaceholderName";
            }

            Integer playerActiveGuildRank = null;
            if (playerGuildRankConfig != null) {
                if (playerGuildRankConfig.contains(uuid.toString()))
                    playerActiveGuildRank = playerGuildRankConfig.getInt(uuid.toString());
            }
            if (playerActiveGuildRank == null) {
                playerActiveGuildRank = 1;
            }

            Integer playerMaxGuildRankValue = null;
            if (playerMaxGuildRankConfig != null) {
                if (playerMaxGuildRankConfig.contains(uuid.toString()))
                    playerMaxGuildRankValue = playerMaxGuildRankConfig.getInt(uuid.toString());
            }
            if (playerMaxGuildRankValue == null) {
                playerMaxGuildRankValue = 1;
            }
            if (playerMaxGuildRankValue == 11) {
                playerActiveGuildRank = 10;
                playerMaxGuildRankValue = 10;
                wasOldMaxRank = true;
            }

            Double currency = null;
            if (playerMoneyDataConfig != null) {
                if (playerMoneyDataConfig.contains(uuid.toString()))
                    currency = playerMoneyDataConfig.getDouble(uuid.toString());
            }
            if (currency == null) {
                currency = 0.0;
            }
            if (wasOldMaxRank)
                currency += 30000;

            try {
                Statement statement = null;
                statement = PlayerData.getConnection().createStatement();
                String sql = "INSERT INTO " + PlayerData.player_data_table_name +
                        " (PlayerUUID, DisplayName, Currency, GuildPrestigeLevel, GuildMaxLevel, GuildActiveLevel) " +
                        //identifier
                        "VALUES ('" + uuid.toString() + "'," +
                        //display name
                        " '" + displayName + "'," +
                        //currency
                        " " + currency + "," +
                        //guild_prestige_level
                        " 0," +
                        //guild_max_level
                        " " + playerMaxGuildRankValue + "," +
                        //guild_active_level
                        " " + playerActiveGuildRank + ");";
                statement.executeUpdate(sql);
                statement.close();
                PlayerData.getConnection().commit();
                PlayerData.getConnection().close();
            } catch (Exception e) {
                new WarningMessage("Warning: Failed to write values from old config files to new database system. Tell the dev!");
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                errored = true;
            }

        }

        if (!errored)
            deleteConfigs(playerCache, playerGuildRank, playerMaxGuildRank, playerMoneyData);

    }

    private void deleteConfigs(File playerCache, File playerGuildRank, File playerMaxGuildRank, File playerMoneyData) {
        deleteConfig(playerCache);
        deleteConfig(playerGuildRank);
        deleteConfig(playerMaxGuildRank);
        deleteConfig(playerMoneyData);
    }

    private void deleteConfig(File file) {
        try {
            if (file.exists() && file.isFile()) {
                file.delete();
                new WarningMessage("Deleted data file " + file.getName() + " - was no longer in use, moved to SQLite");
            }
        } catch (Exception ex) {

        }
    }

}
