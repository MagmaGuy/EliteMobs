package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.ChatColorConverter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
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

        return fileConfiguration;
    }

    public static ItemStack deserialize(String identifier, FileConfiguration fileConfiguration) {
        Material material;
        identifier += ".";
        try {
            material = Material.valueOf(fileConfiguration.getString(identifier + "material"));
        } catch (Exception ex) {
            new WarningMessage("Attempted to add material name " + fileConfiguration.getString("material") + " to a menu. This is not a valid material. Item will default to glass.");
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

        return ItemStackGenerator.generateItemStack(material, name, lore);
    }

}
