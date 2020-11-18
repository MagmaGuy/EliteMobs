package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffectContainer;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemTagger {

    public static final NamespacedKey eliteMobsItemNamespacedKey = new NamespacedKey(MetadataHandler.PLUGIN, "EliteMobsItem");
    public static final NamespacedKey customLore = new NamespacedKey(MetadataHandler.PLUGIN, "CustomLore");

    public static void registerEliteItem(ItemMeta itemMeta) {
        itemMeta.getPersistentDataContainer().set(eliteMobsItemNamespacedKey, PersistentDataType.BYTE, (byte) 1);
    }

    /**
     * Used to register custom lore as a string to the item. This is necessary for the lore updater as it redraws the custom
     * lore portion of the item based on these contents.
     *
     * @param itemMeta ItemStack to which custom lore should be added
     * @param lore     Custom part of the lore to add
     */
    public static void registerCustomLore(ItemMeta itemMeta, List<String> lore) {
        StringBuilder parsedLore = new StringBuilder();
        for (int i = 0; i < lore.size(); i++) {
            parsedLore.append(lore.get(i));
            if (i < lore.size() - 1)
                parsedLore.append("\n");
        }
        itemMeta.getPersistentDataContainer().set(customLore, PersistentDataType.STRING, parsedLore.toString());
    }

    /**
     * Returns any custom lore stored in an item. Returns null if there isn't any.
     *
     * @param itemMeta
     * @return
     */
    public static List<String> getCustomLore(ItemMeta itemMeta) {
        String rawLore = itemMeta.getPersistentDataContainer().get(customLore, PersistentDataType.STRING);
        if (rawLore == null)
            return new ArrayList<>();
        ArrayList<String> parsedLore = new ArrayList();
        for (String entry : rawLore.split("\n"))
            parsedLore.add(entry);
        return parsedLore;
    }

    public static boolean isEliteItem(ItemStack itemStack) {
        if (itemStack == null) return false;
        if (!itemStack.hasItemMeta()) return false;
        if (itemStack.getItemMeta().getCustomTagContainer().hasCustomTag(eliteMobsItemNamespacedKey, ItemTagType.BYTE))
            return true;
        return itemStack.getItemMeta().getPersistentDataContainer().has(eliteMobsItemNamespacedKey, PersistentDataType.BYTE);
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
                registerEnchantment(itemMeta, enchantment.getKey(), enchantmentMap.get(enchantment));
    }

    /**
     * For minecraft enchantments
     *
     * @param itemMeta
     * @param enchantmentKey
     * @param enchantmentLevel
     */
    public static void registerEnchantment(ItemMeta itemMeta, NamespacedKey enchantmentKey, int enchantmentLevel) {
        itemMeta.getPersistentDataContainer().set(enchantmentKey, PersistentDataType.INTEGER, enchantmentLevel);
    }


    public static void registerCustomEnchantments(ItemMeta itemMeta, HashMap<String, Integer> customEnchantments) {
        for (String subString : customEnchantments.keySet())
            registerCustomEnchantment(itemMeta, subString, customEnchantments.get(subString));
    }

    /**
     * For custom enchantments
     *
     * @param itemMeta
     * @param enchantmentKey
     * @param enchantmentLevel
     */
    public static void registerCustomEnchantment(ItemMeta itemMeta, String enchantmentKey, int enchantmentLevel) {
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(MetadataHandler.PLUGIN, enchantmentKey), PersistentDataType.INTEGER, enchantmentLevel);
    }

    public static void registerCustomEnchantment(ItemMeta itemMeta, String enchantmentKey, String uuid) {
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(MetadataHandler.PLUGIN, enchantmentKey), PersistentDataType.STRING, uuid);
    }

    public static int getEnchantment(ItemMeta itemMeta, String enchantmentKey) {
        return getEnchantment(itemMeta, new NamespacedKey(MetadataHandler.PLUGIN, enchantmentKey));
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
        //if (itemMeta.getCustomTagContainer().hasCustomTag(enchantmentKey, ItemTagType.INTEGER))
        //    return itemMeta.getCustomTagContainer().getCustomTag(enchantmentKey, ItemTagType.INTEGER);
        Integer level = itemMeta.getPersistentDataContainer().get(enchantmentKey, PersistentDataType.INTEGER);
        return level == null ? 0 : level;
    }

    public static boolean hasEnchantment(ItemMeta itemMeta, NamespacedKey enchantmentKey) {
        if (!itemMeta.hasLore()) //early performance tweak
            return false;
        if (itemMeta.getCustomTagContainer().hasCustomTag(enchantmentKey, ItemTagType.INTEGER))
            return true;
        return itemMeta.getPersistentDataContainer().has(enchantmentKey, PersistentDataType.INTEGER);
    }

    public static NamespacedKey onHitPotionEffectKey = new NamespacedKey(MetadataHandler.PLUGIN, "onHitPotionEffect");
    public static NamespacedKey continuousPotionEffectKey = new NamespacedKey(MetadataHandler.PLUGIN, "continuousPotionEffect");

    public static ArrayList<ElitePotionEffect> getPotionEffects(ItemMeta itemMeta, NamespacedKey namespacedKey) {
        return ElitePotionEffectContainer.getElitePotionEffectContainer(itemMeta, namespacedKey);
    }

    public static NamespacedKey itemSource = new NamespacedKey(MetadataHandler.PLUGIN, "itemSource");

    public static void registerItemSource(EliteMobEntity eliteMobEntity, ItemMeta itemMeta) {
        if (eliteMobEntity == null) {
            itemMeta.getPersistentDataContainer().set(itemSource, PersistentDataType.STRING, ChatColorConverter.convert(ItemSettingsConfig.shopItemSource));
            return;
        }
        itemMeta.getPersistentDataContainer().set(itemSource, PersistentDataType.STRING, ChatColorConverter.convert(ItemSettingsConfig.mobItemSource.replace("$mob", eliteMobEntity.getName())));
    }

    public static String getItemSource(ItemMeta itemMeta) {
        return itemMeta.getPersistentDataContainer().get(itemSource, PersistentDataType.STRING);
    }

}
