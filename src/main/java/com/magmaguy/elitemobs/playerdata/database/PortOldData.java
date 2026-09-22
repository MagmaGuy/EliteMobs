package com.magmaguy.elitemobs.playerdata.database;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PortOldData {

    public PortOldData() {
        File playerCache = new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/data/playerCache.yml");
        File playerMoneyData = new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/data/playerMoneyData.yml");

        if (!playerCache.exists() && !playerMoneyData.exists())
            return;

        List<PlayerDataRepository.LegacyPlayerData> legacyPlayers = new ArrayList<>();
        try {
            Map<UUID, Object> names = loadLegacyValues(playerCache);
            Map<UUID, Object> balances = loadLegacyValues(playerMoneyData);
            HashSet<UUID> uuids = new HashSet<>(names.keySet());
            uuids.addAll(balances.keySet());

            // Validate both complete sources before the transaction can insert any player.
            for (UUID uuid : uuids) {
                Object name = names.getOrDefault(uuid, "PlaceholderName");
                if (!(name instanceof String displayName)) {
                    throw new InvalidConfigurationException("Invalid display name for " + uuid + " in " + playerCache.getName());
                }
                Object balance = balances.getOrDefault(uuid, 0.0);
                if (!(balance instanceof Number number)) {
                    throw new InvalidConfigurationException("Invalid currency for " + uuid + " in " + playerMoneyData.getName());
                }
                double currency = number.doubleValue();
                double cents = currency * 100.0;
                if (!Double.isFinite(currency) || cents >= 0x1p63 || cents < -0x1p63) {
                    throw new InvalidConfigurationException("Currency is outside the supported range for " + uuid + " in " + playerMoneyData.getName());
                }
                legacyPlayers.add(new PlayerDataRepository.LegacyPlayerData(uuid, displayName, currency));
            }
        } catch (IOException | InvalidConfigurationException exception) {
            Logger.warn("Failed to read legacy player data; no players were imported and source files were preserved.");
            Logger.warn(exception.getClass().getName() + ": " + exception.getMessage());
            return;
        }

        try {
            if (!legacyPlayers.isEmpty()) PlayerDataRepository.importLegacy(legacyPlayers);
            deleteConfigs(playerCache, playerMoneyData);
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

    private Map<UUID, Object> loadLegacyValues(File source) throws IOException, InvalidConfigurationException {
        Map<UUID, Object> values = new HashMap<>();
        if (!source.exists()) return values;
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.load(source);
        for (Map.Entry<String, Object> entry : configuration.getValues(false).entrySet()) {
            UUID uuid;
            try {
                uuid = UUID.fromString(entry.getKey());
            } catch (IllegalArgumentException exception) {
                throw new InvalidConfigurationException("Invalid player UUID '" + entry.getKey() + "' in " + source.getName(), exception);
            }
            if (values.containsKey(uuid)) {
                throw new InvalidConfigurationException("Duplicate player UUID " + uuid + " in " + source.getName());
            }
            values.put(uuid, entry.getValue());
        }
        return values;
    }

}
