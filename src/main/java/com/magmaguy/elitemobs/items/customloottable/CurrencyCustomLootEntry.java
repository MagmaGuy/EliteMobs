package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.ItemLootShower;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.List;

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
            switch (strings[0].toLowerCase()) {
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
                        super.setChance(Integer.parseInt(strings[1]));
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
