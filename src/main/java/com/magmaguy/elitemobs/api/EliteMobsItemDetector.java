package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.items.ObfuscatedSignatureLoreData;
import org.bukkit.inventory.ItemStack;

public class EliteMobsItemDetector {

    public static boolean isEliteMobsItem(ItemStack itemStack) {
        return ObfuscatedSignatureLoreData.obfuscatedSignatureDetector(itemStack);
    }

}
