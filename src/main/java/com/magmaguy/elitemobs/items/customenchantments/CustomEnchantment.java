package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;
import com.magmaguy.elitemobs.config.enchantments.premade.CriticalStrikesConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public abstract class CustomEnchantment {

    /*
    Store key + custom enchantment
     */
    private static HashMap<String, CustomEnchantment> customEnchantments = new HashMap<>();

    public static HashMap<String, CustomEnchantment> getCustomEnchantments() {
        return customEnchantments;
    }

    public static CustomEnchantment getCustomEnchantment(String key) {
        return customEnchantments.get(key);
    }

    private String key;
    private EnchantmentsConfigFields enchantmentsConfigFields;

    public CustomEnchantment(String key) {
        this.key = key;
        this.enchantmentsConfigFields = EnchantmentsConfig.getEnchantment(key + ".yml");
        customEnchantments.put(key, this);
    }

    public String getKey() {
        return key;
    }

    public EnchantmentsConfigFields getEnchantmentsConfigFields() {
        return enchantmentsConfigFields;
    }

    public static void initializeCustomEnchantments() {
        new FlamethrowerEnchantment();
        new HunterEnchantment();
        new CriticalStrikesConfig();
    }

    /*
    Check an itemstack has this enchantment
     */
    public static boolean hasCustomEnchantment(ItemStack itemStack, String key) {
        if (itemStack == null || itemStack.getType().equals(Material.AIR)) return false;
        if (!itemStack.hasItemMeta()) return false;
        if (!itemStack.getItemMeta().hasLore()) return false;
        return hasCustomEnchantment(itemStack.getItemMeta(), key);
    }

    public static boolean hasCustomEnchantment(ItemMeta itemMeta, String key) {
        return ItemTagger.getEnchantment(itemMeta, new NamespacedKey(MetadataHandler.PLUGIN, key)) != 0;
    }

    /*
    Get a custom enchantment's level
     */
    public static int getCustomEnchantmentLevel(ItemStack itemStack, String key) {
        if (itemStack == null) return 0;
        return ItemTagger.getEnchantment(itemStack.getItemMeta(), new NamespacedKey(MetadataHandler.PLUGIN, key));
    }

}
