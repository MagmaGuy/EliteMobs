package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.StaticItemNamesConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.ItemLootShower;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CurrencyCustomLootEntry extends CustomLootEntry implements Serializable {
    private static final long serialVersionUID = -4310216171170221549L;

    @Getter
    private int currencyAmount = 0;

    public CurrencyCustomLootEntry(List<CustomLootEntry> entries, int currencyAmount) {
        this.currencyAmount = currencyAmount;
        entries.add(this);
    }

    public CurrencyCustomLootEntry(List<CustomLootEntry> entries, String rawString, String configFilename) {
        parse(fields(rawString));
        entries.add(this);
    }

    public CurrencyCustomLootEntry(List<CustomLootEntry> entries, Map<?, ?> configMap, String configFilename) {
        parse(fields(configMap));
        entries.add(this);
    }

    private void parse(Map<String, Object> definition) {
        commonFields(definition, true, "currencyamount");
        currencyAmount = integer(definition, "currencyamount");
    }

    @Override
    public boolean locationDrop(int itemTier, Player player, Location location) {
        if (currencyAmount <= 0 || getAmount() <= 0) return false;
        new ItemLootShower(location, player, currencyAmount);
        return true;
    }

    @Override
    public boolean directDrop(int itemTier, Player player) {
        if (currencyAmount <= 0 || getAmount() <= 0) return false;
        EconomyHandler.addCurrency(player.getUniqueId(), currencyAmount);
        player.sendMessage(ItemSettingsConfig.getDirectDropCoinMessage()
                .replace("$amount", currencyAmount + "")
                .replace("$currencyName", EconomySettingsConfig.getCurrencyName()));
        return true;
    }

    @Override
    public ItemStack previewDrop(int itemTier, Player player) {
        ItemStack paperItem = new ItemStack(org.bukkit.Material.PAPER);
        org.bukkit.inventory.meta.ItemMeta paperMeta = paperItem.getItemMeta();
        paperMeta.setDisplayName(StaticItemNamesConfig.getLootPreviewCurrencyDisplayName());
        paperMeta.setLore(List.of(StaticItemNamesConfig.getLootPreviewAmountLabel() + currencyAmount, StaticItemNamesConfig.getLootPreviewChanceLabel() + getChance()));
        paperItem.setItemMeta(paperMeta);
        return paperItem;
    }
}
