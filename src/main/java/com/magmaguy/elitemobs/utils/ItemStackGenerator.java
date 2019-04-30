package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.ChatColorConverter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemStackGenerator {

    public static ItemStack generateItemStack(ItemStack itemStack, String name, List<String> lore) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColorConverter.convert(name));
        itemMeta.setLore(ChatColorConverter.convert(lore));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack generateItemStack(Material material, String name, List<String> lore) {
        ItemStack itemStack = generateItemStack(material, ChatColorConverter.convert(name));
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(ChatColorConverter.convert(lore));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack generateItemStack(Material material, String name) {
        ItemStack itemStack = generateItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack generateItemStack(Material material) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("");
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
