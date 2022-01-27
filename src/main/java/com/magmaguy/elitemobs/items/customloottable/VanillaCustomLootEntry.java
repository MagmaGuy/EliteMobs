package com.magmaguy.elitemobs.items.customloottable;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.List;

public class VanillaCustomLootEntry extends CustomLootEntry implements Serializable {
    @Getter
    private Material material = null;

    public VanillaCustomLootEntry(List<CustomLootEntry> entries, String rawString, String configFilename) {
        parseAllFormats(rawString, configFilename);
        if (this.material == null) return;
        entries.add(this);
    }

    private void parseAllFormats(String rawString, String configFilename) {
        for (String processedString : rawString.split(":")) {
            String[] strings = processedString.split("=");
            switch (strings[0].toLowerCase()) {
                case "type":
                case "material":
                    try {
                        this.material = Material.valueOf(strings[1]);
                    } catch (Exception ex) {
                        errorMessage(rawString, configFilename, "material");
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
            }
        }
    }

    public ItemStack generateItemStack() {
        return new ItemStack(material, getAmount());
    }

    @Override
    public void locationDrop(int itemTier, Player player, Location location) {
        for (int i = 0; i < getAmount(); i++)
            location.getWorld().dropItem(location, generateItemStack());
    }

    @Override
    public void directDrop(int itemTier, Player player) {
        for (int i = 0; i < getAmount(); i++)
            player.getInventory().addItem(generateItemStack());
    }
}
