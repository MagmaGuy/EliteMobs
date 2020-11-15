package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;

public class ItemConstructor {

    public static ItemStack constructItem(String rawName,
                                          Material material,
                                          HashMap<Enchantment, Integer> enchantments,
                                          HashMap<String, Integer> customEnchantments,
                                          List<String> potionEffects,
                                          List<String> lore,
                                          EliteMobEntity eliteMobEntity,
                                          Player player,
                                          boolean showItemWorth) {

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
        if (!enchantments.isEmpty())
            itemMeta = EnchantmentGenerator.generateEnchantments(itemMeta, enchantments);

        /*
        Generate item lore
         */
        itemMeta = LoreGenerator.generateLore(itemMeta, itemMaterial, enchantments, customEnchantments, potionEffects, lore, eliteMobEntity);

        /*
        Remove vanilla enchantments
         */
        if (ItemSettingsConfig.useEliteEnchantments)
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        /*
        Generate item name last as it relies on material type and quality
         */
        itemMeta.setDisplayName(NameGenerator.generateName(rawName));

        /*
        Colorize with MMO colors
         */
        itemStack.setItemMeta(itemMeta);
        ItemQualityColorizer.dropQualityColorizer(itemStack);

        ItemTagger.registerEliteItem(itemStack);

        /*
        Add soulbind if applicable
         */
        SoulbindEnchantment.addEnchantment(itemStack, player);

        /*
        Register item source for lore redraw
         */
        ItemTagger.registerItemSource(eliteMobEntity, itemStack);

        /*
        Update lore
         */
        new EliteItemLore(itemStack, showItemWorth);

        return itemStack;
    }

    /*
    For procedurally generated items
     */
    public static ItemStack constructItem(double itemTier, EliteMobEntity killedMob, Player player, boolean showItemWorth) {

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
        Generate custom enchantments
        Note: This only gets a list of enchantments to be applied later at the lore stage
         */
        HashMap<String, Integer> customEnchantmentMap = EnchantmentGenerator.generateCustomEnchantments(itemTier, itemMaterial);

        /*
        Generate item lore
         */
        itemMeta = LoreGenerator.generateLore(itemMeta, itemMaterial, enchantmentMap, customEnchantmentMap, killedMob);

        /*
        Remove vanilla enchantments
         */
        if (ItemSettingsConfig.useEliteEnchantments)
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

        ItemTagger.registerEliteItem(itemStack);

        /*
        Add soulbind if applicable
         */
        SoulbindEnchantment.addEnchantment(itemStack, player);

        /*
        Register item source for lore redraw
         */
        ItemTagger.registerItemSource(killedMob, itemStack);

        /*
        Update lore
         */
        new EliteItemLore(itemStack, showItemWorth);

        return itemStack;

    }


}
