package com.magmaguy.elitemobs.items.itemconstructor;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;

public class ItemConstructor {

    /*
    For custom items
     */
    public static ItemStack constructItem(String rawItemName, Material material, HashMap<Enchantment,
            Integer> enchantmentMap, List<String> potionList, List<String> loreList) {

        ItemStack itemStack;
        ItemMeta itemMeta;

        /*
        Generate material
         */
        Material itemMaterial = MaterialGenerator.generateMaterial(material);

        /*
        Construct initial item
         */
        itemStack = ItemStackGenerator.generateItemStack(material);
        itemMeta = itemStack.getItemMeta();

        /*
        Generate item enchantments
        Note: This only applies enchantments up to a certain level, above that threshold item enchantments only exist
        in the item lore and get interpreted by the combat system
         */
        itemMeta = EnchantmentGenerator.generateEnchantments(itemMeta, enchantmentMap);

        /*
        Generate item lore
         */
        itemMeta = LoreGenerator.generateLore(itemMeta, itemMaterial, enchantmentMap, potionList, loreList);

        /*
        Remove vanilla enchantments
         */
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        /*
        Generate item name last as it relies on material type and quality
         */
        itemMeta.setDisplayName(NameGenerator.generateName(rawItemName));

        /*
        Colorize with MMO colors
         */
        itemStack.setItemMeta(itemMeta);
        if (itemMeta.getDisplayName().equals(ChatColor.stripColor(itemMeta.getDisplayName())))
            ItemQualityColorizer.dropQualityColorizer(itemStack);

        return itemStack;

    }

    /*
    For procedurally generated items
     */
    public static ItemStack constructItem(double itemTier, LivingEntity killedMob) {

        ItemStack itemStack;
        ItemMeta itemMeta;

        /*
        Generate material
         */
        Material itemMaterial = MaterialGenerator.generateMaterial(itemTier);

        /*
        Construct initial item
         */
        itemStack = ItemStackGenerator.generateItemStack(itemMaterial);
        itemMeta = itemStack.getItemMeta();

         /*
        Generate item enchantments
        Note: This only gets a list of enchantments to be applied later at the lore stage
         */
        HashMap<Enchantment, Integer> enchantmentMap = EnchantmentGenerator.generateEnchantments(itemTier, itemMaterial);

        /*
        Generate item lore
         */
        itemMeta = LoreGenerator.generateLore(itemMeta, itemMaterial, enchantmentMap, killedMob);

        /*
        Remove vanilla enchantments
         */
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        /*
        Generate item name
         */
        itemMeta.setDisplayName(NameGenerator.generateName(itemMaterial));

        /*
        Colorize with MMO colors
         */
        itemStack.setItemMeta(itemMeta);
        ItemQualityColorizer.dropQualityColorizer(itemStack);


        return itemStack;

    }


}
