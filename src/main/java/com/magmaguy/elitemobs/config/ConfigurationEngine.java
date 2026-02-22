package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationEngine extends com.magmaguy.magmacore.config.ConfigurationEngine {

    public static String setString(File file, FileConfiguration fileConfiguration, String key, String defaultValue, boolean translatable) {
        if (translatable && !TranslationsConfig.isEnglish()) {
            // Read existing value (from previous English run) or use default
            String rawValue = fileConfiguration.getString(key);
            String value = rawValue != null ? rawValue : defaultValue;
            // Remove from config so it won't be written to YAML
            fileConfiguration.set(key, null);
            if (value == null) return null;
            return TranslationsConfig.add(file.getName(), key, ChatColorConverter.convert(value));
        }
        fileConfiguration.addDefault(key, defaultValue);
        if (translatable)
            return TranslationsConfig.add(file.getName(), key, ChatColorConverter.convert(fileConfiguration.getString(key)));
        else
            return ChatColorConverter.convert(fileConfiguration.getString(key));
    }

    public static String setString(List<String> comments, File file, FileConfiguration fileConfiguration, String key, String defaultValue, boolean translatable) {
        String value = setString(file, fileConfiguration, key, defaultValue, translatable);
        if (!translatable || TranslationsConfig.isEnglish())
            setComments(fileConfiguration, key, comments);
        return value;
    }

    @SuppressWarnings("unchecked")
    public static List setList(File file, FileConfiguration fileConfiguration, String key, List defaultValue, boolean translatable) {
        if (translatable && !TranslationsConfig.isEnglish()) {
            List<String> rawValue = (List<String>) fileConfiguration.getList(key);
            List<String> value = rawValue != null ? rawValue : (defaultValue != null ? (List<String>) defaultValue : new ArrayList<>());
            fileConfiguration.set(key, null);
            return TranslationsConfig.add(file.getName(), key, value);
        }
        fileConfiguration.addDefault(key, defaultValue);
        if (translatable)
            return TranslationsConfig.add(file.getName(), key, (List<String>) fileConfiguration.getList(key));
        else
            return fileConfiguration.getList(key);
    }

    public static List setList(List<String> comment, File file, FileConfiguration fileConfiguration, String key, List defaultValue, boolean translatable) {
        List value = setList(file, fileConfiguration, key, defaultValue, translatable);
        if (!translatable || TranslationsConfig.isEnglish())
            setComments(fileConfiguration, key, comment);
        return value;
    }

    public static ItemStack setItemStack(File file, FileConfiguration fileConfiguration, String key, ItemStack itemStack, boolean translatable) {
        fileConfiguration.addDefault(key + ".material", itemStack.getType().toString());
        if (TranslationsConfig.isEnglish()) {
            if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
                fileConfiguration.addDefault(key + ".name", itemStack.getItemMeta().getDisplayName());
            if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore())
                fileConfiguration.addDefault(key + ".lore", itemStack.getItemMeta().getLore());
        }
        if (itemStack.getType().equals(Material.PLAYER_HEAD))
            fileConfiguration.addDefault(key + ".owner", ((SkullMeta) itemStack.getItemMeta()).getOwner());
        Material material;
        try {
            material = Material.valueOf(fileConfiguration.getString(key + ".material"));
        } catch (Exception ex) {
            Logger.warn("Material type " + fileConfiguration.getString(key + ".material") + " is not valid! Correct it to make a valid item.");
            return null;
        }
        String name = "";
        try {
            name = setString(file, fileConfiguration, key + ".name", itemStack.getItemMeta().getDisplayName(), true);
        } catch (Exception ex) {
            Logger.warn("Item name " + fileConfiguration.getString(key + ".name") + " is not valid! Correct it to make a valid item.");
        }
        List<String> lore = new ArrayList<>();
        try {
            lore = setList(file, fileConfiguration, key + ".lore", null, true);
        } catch (Exception ex) {
            Logger.warn("Item lore " + fileConfiguration.getString(key + ".lore") + " is not valid! Correct it to make a valid item.");
        }
        ItemStack fileItemStack = ItemStackGenerator.generateItemStack(material, name, lore);
        if (material == Material.PLAYER_HEAD)
            ((SkullMeta) itemStack.getItemMeta()).setOwningPlayer(Bukkit.getOfflinePlayer(fileConfiguration.getString(key + ".owner")));
        return fileItemStack;
    }

}
