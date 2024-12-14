package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomModelAdder {
    public static ItemStack addCustomModel(ItemStack itemStack, String customModelData) {
        if (VersionChecker.serverVersionOlderThan(21,4)) return itemStack;
        if (itemStack == null) return itemStack;
        ItemMeta itemMeta = itemStack.getItemMeta();
        addCustomModel(itemMeta, customModelData);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemMeta addCustomModel(ItemMeta itemMeta, String customModelData) {
        if (VersionChecker.serverVersionOlderThan(21,4) || itemMeta == null) return null;
        itemMeta.setItemModel(NamespacedKey.fromString(customModelData));
        return itemMeta;
    }
}
