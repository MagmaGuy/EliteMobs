package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class EliteScroll {
    public static final NamespacedKey eliteScrollNamespacedKey = new NamespacedKey(MetadataHandler.PLUGIN, "elite_scroll");
    public static Material scrollMaterial = Material.PAPER;

    private EliteScroll() {
    }

    public static ItemStack generateScroll(int scrollLevel) {
        List<String> lore = new ArrayList<>();
        for (String line : ItemSettingsConfig.getEliteItemScrollLore())
            lore.add(line.replace("$level", String.valueOf(scrollLevel)));
        ItemStack itemStack = ItemStackGenerator.generateItemStack(scrollMaterial, ItemSettingsConfig.getEliteItemScrollName(), lore);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.getPersistentDataContainer().set(eliteScrollNamespacedKey, PersistentDataType.INTEGER, scrollLevel);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static boolean isEliteScroll(ItemStack itemStack) {
        if (itemStack == null) return false;
        if (!itemStack.hasItemMeta()) return false;
        return itemStack.getItemMeta().getPersistentDataContainer().has(eliteScrollNamespacedKey);
    }

    public static int getEliteScrollLevel(ItemStack itemStack) {
        if (!isEliteScroll(itemStack)) return 0;
        return itemStack.getItemMeta().getPersistentDataContainer().get(eliteScrollNamespacedKey, PersistentDataType.INTEGER);
    }

    public static ItemStack convertVanillaItem(ItemStack originalItem, ItemStack scrollItemStack) {
        ItemStack newItem = originalItem.clone();
        EliteItemManager.setEliteLevel(newItem, getEliteScrollLevel(scrollItemStack), true);
        return newItem;
    }
}
