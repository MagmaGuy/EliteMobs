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
import java.util.concurrent.ThreadLocalRandom;

public class CommandLootTable extends CustomLootEntry implements Serializable {
    @Getter
    private String command = null;

    public CommandLootTable(List<CustomLootEntry> entries, String rawString, String configFilename) {
        super();
        parseNewFormat(rawString, configFilename);
        entries.add(this);
    }

    //Format: command=my cool command:chance=X.Y:amount=X:permission=per.miss.ion
    private void parseNewFormat(String rawString, String configFilename) {
        for (String string : rawString.split(":")) {
            String[] strings = string.split("=");
            switch (strings[0].toLowerCase(Locale.ROOT)) {
                case "command":
                    try {
                        this.command = strings[1];
                    } catch (Exception ex) {
                        errorMessage(rawString, configFilename, "filename");
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
    }

    //treasure chest
    @Override
    public void locationDrop(int itemTier, Player player, Location location) {
        if (!getPermission().isEmpty() && !player.hasPermission(getPermission())) return;
        if (ThreadLocalRandom.current().nextDouble() < getChance()) return;
        for (int i = 0; i < getAmount(); i++)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
    }

    @Override
    public void directDrop(int itemTier, Player player) {
        if (!getPermission().isEmpty() && !player.hasPermission(getPermission())) return;
        if (ThreadLocalRandom.current().nextDouble() < getChance()) return;
        for (int i = 0; i < getAmount(); i++)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
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
