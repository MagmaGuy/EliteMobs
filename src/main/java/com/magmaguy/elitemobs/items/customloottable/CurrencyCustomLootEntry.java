package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.ItemLootShower;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CurrencyCustomLootEntry extends CustomLootEntry implements Serializable {
    @Getter
    private int currencyAmount = 0;

    public CurrencyCustomLootEntry(List<CustomLootEntry> entries, int currencyAmount) {
        this.currencyAmount = currencyAmount;
        entries.add(this);
    }

    public CurrencyCustomLootEntry(List<CustomLootEntry> entries, String rawString, String configFilename) {
        for (String string : rawString.split(":")) {
            String[] strings = string.split("=");
            switch (strings[0].toLowerCase(Locale.ROOT)) {
                case "currencyamount":
                    try {
                        this.currencyAmount = Integer.parseInt(strings[1]);
                    } catch (Exception ex) {
                        errorMessage(rawString, configFilename, "currencyAmount");
                    }
                    break;
                case "amount":
                    try {
                        super.setAmount(Integer.parseInt(strings[1]));
                    } catch (Exception ex) {
                        errorMessage(rawString, configFilename, "amount");
                    }
                    break;
                case "chance":
                    try {
                        super.setChance(Double.parseDouble(strings[1]));
                    } catch (Exception ex) {
                        errorMessage(rawString, configFilename, "chance");
                    }
                    break;
                case "permission":
                    try {
                        super.setPermission(strings[1]);
                    } catch (Exception ex) {
                        errorMessage(rawString, configFilename, "permission");
                    }
                    break;
                case "wave":
                    try {
                        super.setWave(Integer.parseInt(strings[1]));
                    } catch (Exception ex) {
                        errorMessage(rawString, configFilename, "wave");
                    }
                    break;
                default:
            }
        }
        if (currencyAmount == 0) {
            errorMessage(rawString, configFilename, "currencyAmount");
            return;
        }
        entries.add(this);
    }

    public CurrencyCustomLootEntry(List<CustomLootEntry> entries, Map<?,?> configMap, String configFilename) {
        for (Map.Entry<?, ?> mapEntry : configMap.entrySet()) {
            String key = (String) mapEntry.getKey();
            switch (key.toLowerCase(Locale.ROOT)) {
                case "chance" -> super.setChance(MapListInterpreter.parseDouble(key, mapEntry.getValue(), configFilename));
                case "permission" -> super.setPermission(MapListInterpreter.parseString(key, mapEntry.getValue(), configFilename));
                case "amount" -> setAmount(MapListInterpreter.parseInteger(key, mapEntry.getValue(), configFilename));
                case "currencyamount" -> currencyAmount = MapListInterpreter.parseInteger(key, mapEntry.getValue(), configFilename);
                default -> new WarningMessage("Failed to read custom loot option " + key + " in " + configFilename);
            }
        }
        entries.add(this);
    }


    @Override
    public void locationDrop(int itemTier, Player player, Location location) {
        currencyAmount = (int) (currencyAmount * GuildRank.currencyBonusMultiplier(player.getUniqueId()));
        new ItemLootShower(location, player, currencyAmount);
    }

    @Override
    public void directDrop(int itemTier, Player player) {
        EconomyHandler.addCurrency(player.getUniqueId(), currencyAmount * GuildRank.currencyBonusMultiplier(player.getUniqueId()));
        player.sendMessage(ItemSettingsConfig.getDirectDropCoinMessage()
                .replace("$amount", currencyAmount + "")
                .replace("$currencyName", EconomySettingsConfig.getCurrencyName()));
    }
}
