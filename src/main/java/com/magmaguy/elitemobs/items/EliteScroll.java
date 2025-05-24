package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.utils.CustomModelAdder;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
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

    public static ItemStack generateScroll(int scrollLevel, Player player) {
        // Generate base item
        ItemStack itemStack = ItemStackGenerator.generateItemStack(scrollMaterial, ItemSettingsConfig.getEliteItemScrollName());

        // Get meta once and reuse
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return itemStack; // Safety check

        // Set persistent data
        itemMeta.getPersistentDataContainer().set(eliteScrollNamespacedKey, PersistentDataType.INTEGER, scrollLevel);

        // Add custom model
        CustomModelAdder.addCustomModel(itemMeta, ItemSettingsConfig.getEliteItemModel());

        itemStack.setItemMeta(itemMeta);

        // Register and add enchantments after meta is set
        ItemTagger.registerEliteItem(itemStack);
        SoulbindEnchantment.addEnchantment(itemStack, player);

        itemMeta = itemStack.getItemMeta();

        // Build lore list - prepend new lore
        List<String> existingLore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
        List<String> lore = new ArrayList<>();


        // Add new lore first
        for (String line : ItemSettingsConfig.getEliteItemScrollLore()) {
            lore.add(ChatColorConverter.convert(line.replace("$level", String.valueOf(scrollLevel))));
        }

        // Then add existing lore
        lore.addAll(existingLore);
        itemMeta.setLore(lore);

        // Apply meta changes once
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
        Player player = SoulbindEnchantment.getSoulboundPlayer(scrollItemStack.getItemMeta());
        ItemStack newItem = originalItem.clone();
        ItemTagger.registerEliteItem(newItem);
        EliteItemManager.setEliteLevel(newItem, getEliteScrollLevel(scrollItemStack), true);
        if (player != null) SoulbindEnchantment.addEnchantment(newItem, player);
        return newItem;
    }
}
