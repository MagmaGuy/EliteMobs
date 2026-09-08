package com.magmaguy.elitemobs.items.upgradesystem;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.customenchantments.EnchantedSourceEnchantment;
import com.magmaguy.elitemobs.items.customenchantments.MagicWeaponEnchantment;
import com.magmaguy.elitemobs.items.itemconstructor.EnchantmentGenerator;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class UpgradeSystem {
    private UpgradeSystem() {
    }

    public static ItemStack upgrade(ItemStack originalItemToUpgrade, ItemStack enchantedBook) {
        if (!isCompatibleBook(originalItemToUpgrade, enchantedBook))
            throw new IllegalArgumentException("The book contains enchantments incompatible with this item");
        ItemStack itemToUpgrade = originalItemToUpgrade.clone();
        ItemMeta itemMeta = itemToUpgrade.getItemMeta();
        Map<NamespacedKey, Integer> currentEnchantments = ItemTagger.getItemEnchantments(itemToUpgrade);
        Map<NamespacedKey, Integer> bookEnchantments = ItemTagger.getItemEnchantments(enchantedBook);
        Map<NamespacedKey, Integer> newMap = new HashMap<>(currentEnchantments);
        //Remove the enchantment that makes this a book, it's non-transferable
        bookEnchantments.remove(new NamespacedKey(MetadataHandler.PLUGIN, EnchantedSourceEnchantment.key));

        //merge enchants
        for (Map.Entry<NamespacedKey, Integer> entrySet : bookEnchantments.entrySet()) {
            if (!currentEnchantments.containsKey(entrySet.getKey()))
                newMap.put(entrySet.getKey(), entrySet.getValue());
            else
                newMap.put(entrySet.getKey(), currentEnchantments.get(entrySet.getKey()) + entrySet.getValue());
        }

        //get vanilla enchants
        Map<NamespacedKey, Integer> vanillaEnchantmentsNamespaced = new HashMap<>();
        HashMap<Enchantment, Integer> vanillaEnchantments = new HashMap<>();
        for (Map.Entry<NamespacedKey, Integer> entrySet : newMap.entrySet()) {
            Enchantment enchantment = Enchantment.getByKey(entrySet.getKey());
            if (enchantment != null) {
                vanillaEnchantments.put(enchantment, entrySet.getValue());
                vanillaEnchantmentsNamespaced.put(entrySet.getKey(), entrySet.getValue());
            }
        }

        EnchantmentGenerator.generateEnchantments(itemMeta, vanillaEnchantments);
        ItemTagger.registerEnchantments(itemMeta, vanillaEnchantments);

        //get custom enchantments
        HashMap<String, Integer> customEnchantments = new HashMap<>();
        for (Map.Entry<NamespacedKey, Integer> entrySet : newMap.entrySet())
            if (!vanillaEnchantmentsNamespaced.containsKey(entrySet.getKey()))
                customEnchantments.put(entrySet.getKey().getKey(), entrySet.getValue());

        ItemTagger.registerCustomEnchantments(itemMeta, customEnchantments);

        itemToUpgrade.setItemMeta(itemMeta);
        new EliteItemLore(itemToUpgrade, false);
        return itemToUpgrade;
    }

    public static boolean isValidUpgrade(ItemStack originalItemToUpgrade, ItemStack enchantedBook) {
        if (!isCompatibleBook(originalItemToUpgrade, enchantedBook)) return false;
        ItemStack finalItemStack = upgrade(originalItemToUpgrade, enchantedBook);
        Map<EnchantmentsConfigFields, Integer> currentEnchantments = ItemTagger.getItemEnchantmentConfigFields(finalItemStack);
        for (Map.Entry<EnchantmentsConfigFields, Integer> entry : currentEnchantments.entrySet())
            if (entry.getKey() == null || entry.getValue() > entry.getKey().getMaxEnchantmentLevel()
                    || (MagicWeaponEnchantment.KEYS.contains(entry.getKey().getFilename().replace(".yml", ""))
                    && entry.getValue() > 3)) return false;
        return true;
    }

    public static boolean isCompatibleBook(ItemStack item, ItemStack book) {
        if (item == null || book == null) return false;
        Map<NamespacedKey, Integer> additions = ItemTagger.getItemEnchantments(book);
        additions.remove(new NamespacedKey(MetadataHandler.PLUGIN, EnchantedSourceEnchantment.key));
        if (additions.isEmpty()) return false;
        for (NamespacedKey key : additions.keySet())
            if (!MagicWeaponEnchantment.compatible(item, key)) return false;
        return true;
    }

}
