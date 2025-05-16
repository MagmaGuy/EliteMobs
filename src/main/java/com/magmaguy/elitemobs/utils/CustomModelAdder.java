package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.config.CustomModelsConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomModelAdder {
    public static ItemStack addCustomModel(ItemStack itemStack, String customModelData) {
        if (itemStack == null || itemStack.getItemMeta() == null ||
                customModelData == null || customModelData.isEmpty()) return itemStack;
        itemStack.setItemMeta(addCustomModel(itemStack.getItemMeta(), customModelData));
        return itemStack;
    }

    public static ItemMeta addCustomModel(ItemMeta itemMeta, String customModelData) {
        if (!CustomModelsConfig.useModels) return itemMeta;
        if (!DefaultConfig.useResourcePackModels()) return itemMeta;
        if (VersionChecker.serverVersionOlderThan(21,4) || itemMeta == null) return itemMeta;
        itemMeta.setItemModel(NamespacedKey.fromString(customModelData));
        return itemMeta;
    }
}
