package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.utils.ObjectSerializer;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.List;

public class ItemStackCustomLootEntry extends CustomLootEntry implements Serializable {
    private final String serializedItemStack;

    public ItemStackCustomLootEntry(List<CustomLootEntry> entries, ItemStack itemStack) {
        super();
        this.serializedItemStack = ObjectSerializer.itemStackArrayToBase64(itemStack);
        entries.add(this);
    }

    public ItemStack generateItemStack() {
        try {
            return ObjectSerializer.itemStackArrayFromBase64(serializedItemStack);
        } catch (Exception ex) {
            Logger.warn("Failed to serialize item stack from Custom Loot Table");
            return null;
        }
    }

    @Override
    public boolean directDrop(int itemTier, Player player) {
        ItemStack itemStack = generateItemStack();
        if (itemStack == null || itemStack.getType().isAir() || itemStack.getAmount() <= 0 || getAmount() <= 0) return false;
        for (int i = 0; i < getAmount(); i++) {
            var overflow = player.getInventory().addItem(itemStack.clone());
            overflow.values().forEach(leftover -> player.getWorld().dropItem(player.getLocation(), leftover));
        }
        return true;
    }

    @Override
    public boolean locationDrop(int itemTier, Player player, Location location) {
        ItemStack itemStack = generateItemStack();
        if (itemStack == null || itemStack.getType().isAir() || itemStack.getAmount() <= 0 || getAmount() <= 0) return false;
        for (int i = 0; i < getAmount(); i++)
            location.getWorld().dropItem(location, itemStack.clone());
        return true;
    }

    @Override
    public ItemStack previewDrop(int itemTier, Player player) {
        return generateItemStack();
    }
}
