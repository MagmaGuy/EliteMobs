package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffectContainer;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;

import java.util.ArrayList;
import java.util.HashMap;

public class ItemTagger {

    public static final NamespacedKey eliteMobsItemNamespacedKey = new NamespacedKey(MetadataHandler.PLUGIN, "EliteMobsItem");

    public static void registerEliteItem(ItemStack itemStack) {
        itemStack.getItemMeta().getCustomTagContainer().setCustomTag(eliteMobsItemNamespacedKey, ItemTagType.BYTE, (byte) 1);
    }

    public static boolean isEliteItem(ItemStack itemStack) {
        if (itemStack == null) return false;
        if (!itemStack.hasItemMeta()) return false;
        return itemStack.getItemMeta().getCustomTagContainer().hasCustomTag(eliteMobsItemNamespacedKey, ItemTagType.BYTE);
    }

    /**
     * Registers all enchantments as they'd appear when constructing an item in EliteMobs
     *
     * @param itemMeta
     * @param enchantmentMap
     */
    public static void registerEnchantments(ItemMeta itemMeta, HashMap<Enchantment, Integer> enchantmentMap) {
        for (Enchantment enchantment : enchantmentMap.keySet())
            if (EliteEnchantments.isPotentialEliteEnchantment(enchantment))
                ItemTagger.registerEnchantment(itemMeta, enchantment.getKey(), enchantmentMap.get(enchantment));
    }

    /**
     * For minecraft enchantments
     *
     * @param itemMeta
     * @param enchantmentKey
     * @param enchantmentLevel
     */
    public static void registerEnchantment(ItemMeta itemMeta, NamespacedKey enchantmentKey, int enchantmentLevel) {
        itemMeta.getCustomTagContainer().setCustomTag(enchantmentKey, ItemTagType.INTEGER, enchantmentLevel);
    }


    public static void registerCustomEnchantments(ItemMeta itemMeta, HashMap<String, Integer> customEnchantments) {
        for (String subString : customEnchantments.keySet())
            ItemTagger.registerCustomEnchantment(itemMeta, subString, customEnchantments.get(subString));
    }

    /**
     * For custom enchantments
     *
     * @param itemMeta
     * @param enchantmentKey
     * @param enchantmentLevel
     */
    public static void registerCustomEnchantment(ItemMeta itemMeta, String enchantmentKey, int enchantmentLevel) {
        itemMeta.getCustomTagContainer().setCustomTag(new NamespacedKey(MetadataHandler.PLUGIN, enchantmentKey),
                ItemTagType.INTEGER, enchantmentLevel);
    }

    public static void registerCustomEnchantment(ItemMeta itemMeta, String enchantmentKey, String uuid) {
        itemMeta.getCustomTagContainer().setCustomTag(new NamespacedKey(MetadataHandler.PLUGIN, enchantmentKey),
                ItemTagType.STRING, uuid);
    }

    /**
     * Returns the level of that enchantment on the item. 0 means the enchantment is not present.
     *
     * @param itemMeta
     * @param enchantmentKey
     * @return
     */
    public static int getEnchantment(ItemMeta itemMeta, NamespacedKey enchantmentKey) {
        if (itemMeta == null)
            return 0;
        if (!itemMeta.getCustomTagContainer().hasCustomTag(enchantmentKey, ItemTagType.INTEGER))
            return 0;
        return itemMeta.getCustomTagContainer().getCustomTag(enchantmentKey, ItemTagType.INTEGER);
    }

    public static boolean hasEnchantment(ItemMeta itemMeta, NamespacedKey enchantmentKey) {
        return itemMeta.getCustomTagContainer().hasCustomTag(enchantmentKey, ItemTagType.INTEGER);
    }

    public static NamespacedKey onHitPotionEffectKey = new NamespacedKey(MetadataHandler.PLUGIN, "onHitPotionEffect");
    public static NamespacedKey continuousPotionEffectKey = new NamespacedKey(MetadataHandler.PLUGIN, "continuousPotionEffect");

    public static ArrayList<ElitePotionEffect> getPotionEffects(ItemMeta itemMeta, NamespacedKey namespacedKey) {
        return ElitePotionEffectContainer.getElitePotionEffectContainer(itemMeta, namespacedKey);
    }

}
