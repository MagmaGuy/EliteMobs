package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.utils.ObjectSerializer;
import com.magmaguy.elitemobs.utils.WarningMessage;
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
            new WarningMessage("Failed to serialize item stack from Custom Loot Table");
            return null;
        }
    }

    @Override
    public void directDrop(int itemTier, Player player) {
        ItemStack itemStack = generateItemStack();
        if (itemStack == null) return;
        for (int i = 0; i < getAmount(); i++)
            player.getInventory().addItem(itemStack);
    }
}
