package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.items.itemconstructor.SpecialLoot;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.List;

public class SpecialCustomLootEntry extends CustomLootEntry implements Serializable {
    @Getter
    private String rawString;
    private String configFilename;

    public SpecialCustomLootEntry(List<CustomLootEntry> entries, String rawString, String configFilename) {
        this.rawString = rawString;
        this.configFilename = configFilename;
        entries.add(this);
    }

    private SpecialLoot generateSpecialLoot() {
        return new SpecialLoot(rawString, configFilename);
    }

    public ItemStack generateItemStack(Player player){
        return generateSpecialLoot().generateItemStack(player);
    }

    @Override
    public void locationDrop(int itemTier, Player player, Location location) {
        location.getWorld().dropItem(location, generateSpecialLoot().generateItemStack(player));
    }

    @Override
    public void directDrop(int itemTier, Player player) {
        player.getInventory().addItem(generateSpecialLoot().generateItemStack(player));
    }
}
