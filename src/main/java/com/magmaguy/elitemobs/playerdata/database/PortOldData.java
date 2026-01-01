package com.magmaguy.elitemobs.playerdata.database;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.Statement;
import java.util.HashSet;
import java.util.UUID;

public class PortOldData {

    public PortOldData() {
        File playerCache = new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/data/playerCache.yml");
        File playerMoneyData = new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/data/playerMoneyData.yml");

        if (!playerCache.isFile() && !playerMoneyData.isFile())
            return;

        FileConfiguration playerCacheConfig = null,
                playerMoneyDataConfig = null;

        HashSet<UUID> uuids = new HashSet<>();

        if (playerCache.exists()) {
            playerCacheConfig = YamlConfiguration.loadConfiguration(playerCache);
            for (String string : playerCacheConfig.getKeys(false))
                uuids.add(UUID.fromString(string));
        }

        if (playerMoneyData.exists()) {
            playerMoneyDataConfig = YamlConfiguration.loadConfiguration(playerMoneyData);
            for (String string : playerMoneyDataConfig.getKeys(false))
                uuids.add(UUID.fromString(string));
        }

        if (uuids.isEmpty()) {
            deleteConfigs(playerCache, playerMoneyData);
            return;
        }

        boolean errored = false;

        for (UUID uuid : uuids) {

            String displayName = null;
            if (playerCacheConfig != null) {
                if (playerCacheConfig.contains(uuid.toString()))
                    displayName = playerCacheConfig.getString(uuid.toString());
            }
            if (displayName == null) {
                displayName = "PlaceholderName";
            }

            Double currency = null;
            if (playerMoneyDataConfig != null) {
                if (playerMoneyDataConfig.contains(uuid.toString()))
                    currency = playerMoneyDataConfig.getDouble(uuid.toString());
            }
            if (currency == null) {
                currency = 0.0;
            }

            try {
                Statement statement = null;
                statement = PlayerData.getConnection().createStatement();
                String sql = "INSERT INTO " + PlayerData.getPLAYER_DATA_TABLE_NAME() +
                        " (PlayerUUID, DisplayName, Currency) " +
                        //identifier
                        "VALUES ('" + uuid.toString() + "'," +
                        //display name
                        " '" + displayName + "'," +
                        //currency
                        " " + currency + ");";
                statement.executeUpdate(sql);
                statement.close();
                PlayerData.getConnection().commit();
                PlayerData.getConnection().close();
            } catch (Exception e) {
                Logger.warn("Warning: Failed to write values from old config files to new database system. Tell the dev!");
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                errored = true;
            }

        }

        if (!errored)
            deleteConfigs(playerCache, playerMoneyData);

    }

    private void deleteConfigs(File playerCache, File playerMoneyData) {
        deleteConfig(playerCache);
        deleteConfig(playerMoneyData);
    }

    private void deleteConfig(File file) {
        try {
            if (file.exists() && file.isFile()) {
                file.delete();
                Logger.warn("Deleted data file " + file.getName() + " - was no longer in use, moved to SQLite");
            }
        } catch (Exception ex) {

        }
    }

}
