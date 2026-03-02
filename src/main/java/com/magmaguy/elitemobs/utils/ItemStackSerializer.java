package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
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
        return deserialize(identifier, fileConfiguration, null);
    }

    public static ItemStack deserialize(String identifier, FileConfiguration fileConfiguration, File file) {
        Material material;
        String fullId = identifier + ".";
        try {
            material = Material.valueOf(fileConfiguration.getString(fullId + "material"));
        } catch (Exception ex) {
            Logger.warn("Attempted to add material name " + fileConfiguration.getString("material") + " to a menu. This is not a valid material. Item will default to glass.");
            material = Material.RED_STAINED_GLASS_PANE;
        }

        String name = "";
        if (fileConfiguration.contains(fullId + "name")) {
            if (file != null) {
                String rawName = fileConfiguration.getString(fullId + "name");
                if (!TranslationsConfig.isEnglish()) {
                    fileConfiguration.set(fullId + "name", null);
                    Configuration defaults = fileConfiguration.getDefaults();
                    if (defaults != null) defaults.set(fullId + "name", null);
                }
                name = TranslationsConfig.add(file.getName(), identifier + ".name", rawName);
            } else {
                name = ChatColorConverter.convert(fileConfiguration.getString(fullId + "name"));
            }
        }

        List<String> lore = new ArrayList<>();
        if (fileConfiguration.contains(fullId + "lore")) {
            if (file != null) {
                List<String> rawLore = fileConfiguration.getStringList(fullId + "lore");
                if (!TranslationsConfig.isEnglish()) {
                    fileConfiguration.set(fullId + "lore", null);
                    Configuration defaults = fileConfiguration.getDefaults();
                    if (defaults != null) defaults.set(fullId + "lore", null);
                }
                lore = TranslationsConfig.add(file.getName(), identifier + ".lore", rawLore);
            } else {
                lore = ChatColorConverter.convert(fileConfiguration.getStringList(fullId + "lore"));
            }
        }

        if (material.equals(Material.PLAYER_HEAD)) {
            String owner = fileConfiguration.getString(fullId + "owner");
            return ItemStackGenerator.generateSkullItemStack(owner, name, lore);
        }

        int customModelID = 0;
        if (fileConfiguration.contains(fullId + "customModelID")) {
            customModelID = fileConfiguration.getInt(fullId + "customModelID");
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
