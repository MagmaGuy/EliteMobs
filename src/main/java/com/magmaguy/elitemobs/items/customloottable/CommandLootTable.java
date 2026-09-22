package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.config.StaticItemNamesConfig;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

public class CommandLootTable extends CustomLootEntry implements Serializable {
    private static final long serialVersionUID = -8058067487928551461L;

    @Getter
    private String command = null;

    public CommandLootTable(List<CustomLootEntry> entries, String rawString, String configFilename) {
        this(entries, fields(rawString), configFilename);
    }

    CommandLootTable(List<CustomLootEntry> entries, java.util.Map<String, Object> definition, String configFilename) {
        commonFields(definition, true, "command");
        command = text(definition, "command");
        entries.add(this);
    }

    //treasure chest
    @Override
    public boolean locationDrop(int itemTier, Player player, Location location) {
        return directDrop(itemTier, player);
    }

    @Override
    public boolean directDrop(int itemTier, Player player) {
        if (command == null || command.isBlank() || getAmount() <= 0) return false;
        if (!getPermission().isEmpty() && !player.hasPermission(getPermission())) return false;
        boolean dispatched = false;
        for (int i = 0; i < getAmount(); i++)
            dispatched |= Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
        return dispatched;
    }

    @Override
    public ItemStack previewDrop(int itemTier, Player player) {
        ItemStack paperItem = new ItemStack(org.bukkit.Material.PAPER);
        org.bukkit.inventory.meta.ItemMeta paperMeta = paperItem.getItemMeta();
        paperMeta.setDisplayName(StaticItemNamesConfig.getLootPreviewCommandDisplayName());
        paperMeta.setLore(List.of(StaticItemNamesConfig.getLootPreviewCommandLabel() + command, StaticItemNamesConfig.getLootPreviewChanceLabel() + getChance(), StaticItemNamesConfig.getLootPreviewTimesLabel() + getAmount()));
        paperItem.setItemMeta(paperMeta);
        return paperItem;
    }
}
