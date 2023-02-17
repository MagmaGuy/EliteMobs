package com.magmaguy.elitemobs.items.upgradesystem;

import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.customenchantments.CustomEnchantment;
import com.magmaguy.elitemobs.items.customenchantments.EnchantedSourceEnchantment;
import com.magmaguy.elitemobs.items.customenchantments.LuckySourceEnchantment;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class EliteEnchantmentItems {

    public static Map<String, Integer> getEnchantedBookEnchantments(@Nullable ItemStack itemStack) {
        Map<String, Integer> enchantMap = new HashMap<>();
        if (itemStack == null || itemStack.getItemMeta() == null) return enchantMap;
        CustomEnchantment.getCustomEnchantmentMap().values();
        return enchantMap;
    }

    public static boolean isEliteEnchantmentBook(@Nullable ItemStack itemStack) {
        if (itemStack == null || itemStack.getItemMeta() == null) return false;
        return ItemTagger.hasEnchantment(itemStack.getItemMeta(), EnchantedSourceEnchantment.key);
    }

    public static boolean isEliteLuckyTicket(@Nullable ItemStack itemStack) {
        if (itemStack == null || itemStack.getItemMeta() == null) return false;
        return ItemTagger.hasEnchantment(itemStack.getItemMeta(), LuckySourceEnchantment.key);
    }

}
