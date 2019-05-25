package com.magmaguy.elitemobs.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ObfuscatedSignatureLoreData {

    public static final String ITEM_SIGNATURE = "EliteItem";

    public static boolean obfuscatedSignatureDetector(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().equals(Material.AIR) || !itemStack.getItemMeta().hasLore())
            return false;
        return itemStack.getItemMeta().getLore().get(0).replace("§", "").contains(ITEM_SIGNATURE);
    }

}
