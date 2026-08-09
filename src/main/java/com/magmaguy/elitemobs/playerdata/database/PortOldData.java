package com.magmaguy.elitemobs.playerdata.database;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        boolean invalidKeys = false;

        if (playerCache.exists()) {
            playerCacheConfig = YamlConfiguration.loadConfiguration(playerCache);
            invalidKeys |= collectValidUuids(playerCacheConfig, uuids, playerCache.getName());
        }

        if (playerMoneyData.exists()) {
            playerMoneyDataConfig = YamlConfiguration.loadConfiguration(playerMoneyData);
            invalidKeys |= collectValidUuids(playerMoneyDataConfig, uuids, playerMoneyData.getName());
        }

        if (uuids.isEmpty() && !invalidKeys) {
            deleteConfigs(playerCache, playerMoneyData);
            return;
        }

        List<PlayerDataRepository.LegacyPlayerData> legacyPlayers = new ArrayList<>();
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

            legacyPlayers.add(new PlayerDataRepository.LegacyPlayerData(uuid, displayName, currency));
        }

        try {
            PlayerDataRepository.importLegacy(legacyPlayers);
            if (!invalidKeys) deleteConfigs(playerCache, playerMoneyData);
            else Logger.warn("Legacy player files were preserved because they contain invalid UUID keys.");
        } catch (Exception exception) {
            Logger.warn("Failed to transactionally import legacy player data; source files were preserved.");
            Logger.warn(exception.getClass().getName() + ": " + exception.getMessage());
        }

    }

    private void deleteConfigs(File playerCache, File playerMoneyData) {
        deleteConfig(playerCache);
        deleteConfig(playerMoneyData);
    }

    private void deleteConfig(File file) {
        if (!file.exists() || !file.isFile()) return;
        if (file.delete()) {
            Logger.warn("Deleted data file " + file.getName() + " - was no longer in use, moved to the player database");
        } else {
            Logger.warn("Legacy player data was imported, but " + file.getName() + " could not be deleted.");
        }
    }

    private boolean collectValidUuids(FileConfiguration configuration, HashSet<UUID> uuids, String sourceName) {
        boolean invalid = false;
        for (String key : configuration.getKeys(false)) {
            try {
                uuids.add(UUID.fromString(key));
            } catch (IllegalArgumentException exception) {
                invalid = true;
                Logger.warn("Ignoring invalid player UUID '" + key + "' in " + sourceName + ".");
            }
        }
        return invalid;
    }

}
