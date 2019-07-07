package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.ChatColorConverter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class ItemStackGenerator {

    public static ItemStack generateSkullItemStack(String owner, String name, List<String> lore) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        skullMeta.setOwner(owner);
        skullMeta.setDisplayName(name);
        skullMeta.setLore(lore);
        itemStack.setItemMeta(skullMeta);
        return itemStack;
    }

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
        if (material == null) material = Material.AIR;
        ItemStack itemStack = new ItemStack(material);
        if (material.equals(Material.AIR)) return itemStack;
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("");
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
