package com.magmaguy.elitemobs.utils;

import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemStackSerializer {

    public static FileConfiguration serialize(String identifier, ItemStack itemStack, FileConfiguration fileConfiguration) {
        fileConfiguration.addDefault(identifier + ".material", itemStack.getType().toString());
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
            fileConfiguration.addDefault(identifier + ".name", itemStack.getItemMeta().getDisplayName());
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore())
            fileConfiguration.addDefault(identifier + ".lore", itemStack.getItemMeta().getLore());
        if (itemStack.getType().equals(Material.PLAYER_HEAD))
            fileConfiguration.addDefault(identifier + ".owner", ((SkullMeta) itemStack.getItemMeta()).getOwner());
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasCustomModelData() && itemStack.getItemMeta().getCustomModelData() != 0)
            fileConfiguration.addDefault(identifier + ".customModelID", itemStack.getItemMeta().getCustomModelData());

        return fileConfiguration;
    }

    public static ItemStack deserialize(String identifier, FileConfiguration fileConfiguration) {
        Material material;
        identifier += ".";
        try {
            material = Material.valueOf(fileConfiguration.getString(identifier + "material"));
        } catch (Exception ex) {
            Logger.warn("Attempted to add material name " + fileConfiguration.getString("material") + " to a menu. This is not a valid material. Item will default to glass.");
            material = Material.RED_STAINED_GLASS_PANE;
        }

        String name = "";
        if (fileConfiguration.contains(identifier + "name"))
            name = ChatColorConverter.convert(fileConfiguration.getString(identifier + "name"));

        List<String> lore = new ArrayList<>();
        if (fileConfiguration.contains(identifier + "lore"))
            lore = ChatColorConverter.convert(fileConfiguration.getStringList(identifier + "lore"));

        if (material.equals(Material.PLAYER_HEAD)) {
            String owner = fileConfiguration.getString(identifier + "owner");
            return ItemStackGenerator.generateSkullItemStack(owner, name, lore);
        }

        int customModelID = 0;
        if (fileConfiguration.contains(identifier + "customModelID")) {
            customModelID = fileConfiguration.getInt(identifier + "customModelID");
        }

        return ItemStackGenerator.generateItemStack(material, name, lore, customModelID);
    }

    /**
     * Used to generate itemstacks from configuration files when they contain placeholders
     *
     * @param itemStack
     * @param placeholderReplacementPairs List of placeholders / replacements. Each placeholder is a key, each key has 1 replacement value.
     * @return
     */
    public static ItemStack itemStackPlaceholderReplacer(ItemStack itemStack, HashMap<String, String> placeholderReplacementPairs) {
        ItemStack cloneItemStack = itemStack.clone();
        ItemMeta newMeta = itemStack.getItemMeta();


        //Replace placeholders in name
        if (newMeta.hasDisplayName()) {
            String newName = newMeta.getDisplayName();
            for (String placeholder : placeholderReplacementPairs.keySet())
                if (newMeta.getDisplayName().contains(placeholder))
                    newName = newName.replace(placeholder, placeholderReplacementPairs.get(placeholder));
            newMeta.setDisplayName(newName);
        }

        List<String> newLore = new ArrayList<>();
        if (newMeta.hasLore()) {
            for (String loreString : newMeta.getLore()) {
                String newLoreString = loreString;
                for (String placeholder : placeholderReplacementPairs.keySet())
                    if (loreString.contains(placeholder))
                        newLoreString = newLoreString.replace(placeholder, placeholderReplacementPairs.get(placeholder));
                newLore.add(newLoreString);
            }
            newMeta.setLore(newLore);
        }

        cloneItemStack.setItemMeta(newMeta);
        return cloneItemStack;
    }

}
