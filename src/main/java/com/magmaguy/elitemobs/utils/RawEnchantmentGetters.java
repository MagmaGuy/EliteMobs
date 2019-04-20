package com.magmaguy.elitemobs.utils;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class RawEnchantmentGetters {

    /**
     * Catches the obfuscated levels which are the values EliteMobs works with. Should only get called if the item
     * has the signature obfuscated lore.
     *
     * @param itemStack ItemStack to get analysed
     * @return HashMap of all obfuscated enchantments
     */
    public static Map<Enchantment, Integer> vanillaEnchantmentsList(ItemStack itemStack) {

        return vanillaEnchantmentsList(itemStack.getEnchantments(), itemStack.getItemMeta().getLore().get(0));

    }

    public static Map<Enchantment, Integer> vanillaEnchantmentsList(Map<Enchantment, Integer> actualEnchantmentsList, String obfuscatedLore) {

        Map<Enchantment, Integer> enchantmentsMap = new HashMap();

        if (actualEnchantmentsList.isEmpty())
            return enchantmentsMap;

        for (Enchantment enchantment : actualEnchantmentsList.keySet())
            enchantmentsMap.put(enchantment, getDeobfuscatedLevel(obfuscatedLore, enchantment.getName()));

        return enchantmentsMap;

    }

    private static int getDeobfuscatedLevel(String obfuscatedLore, String enchantmentName) {

        String deobfuscatedLore = obfuscatedLore.replace("§", "");

        for (String stringPairs : deobfuscatedLore.split(",")) {
            String[] stringList = stringPairs.split(":");
            if (stringList[0].equalsIgnoreCase(enchantmentName))
                return Integer.parseInt(stringList[1]);
        }

        return 0;

    }

}
