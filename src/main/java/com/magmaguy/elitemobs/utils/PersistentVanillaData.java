package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PersistentVanillaData {

    public static void write(Entity entity, String key, String value) {
        entity.getPersistentDataContainer().set(getKey(key), PersistentDataType.STRING, value);
    }

    public static void write(ItemStack itemStack, String key, double value) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.getPersistentDataContainer().set(getKey(key), PersistentDataType.DOUBLE, value);
        itemStack.setItemMeta(itemMeta);
    }

    public static boolean hasString(Entity entity, String key) {
        return entity.getPersistentDataContainer().has(getKey(key), PersistentDataType.STRING);
    }

    public static String getString(Entity entity, String key) {
        return entity.getPersistentDataContainer().get(getKey(key), PersistentDataType.STRING);
    }

    public static double getDouble(ItemStack itemStack, String key) {
        if (itemStack == null || itemStack.getItemMeta() == null) return -1;
        if (!itemStack.getItemMeta().getPersistentDataContainer().has(getKey(key), PersistentDataType.DOUBLE))
            return -1;
        Double d = itemStack.getItemMeta().getPersistentDataContainer().get(getKey(key), PersistentDataType.DOUBLE);
        return d == null ? -1 : d;
    }

    private static NamespacedKey getKey(String key) {
        return new NamespacedKey(MetadataHandler.PLUGIN, key);
    }

}
