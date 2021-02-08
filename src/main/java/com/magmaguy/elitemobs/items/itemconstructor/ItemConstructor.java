package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffectContainer;
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
        Construct initial item
         */
        itemStack = ItemStackGenerator.generateItemStack(material);
        itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(ChatColorConverter.convert(rawName));

        /*
        Generate item enchantments
        Note: This only applies enchantments up to a certain level, above that threshold item enchantments only exist
        in the item lore and get interpreted by the combat system
         */
        if (!enchantments.isEmpty())
            EnchantmentGenerator.generateEnchantments(itemMeta, enchantments);

        itemStack.setItemMeta(itemMeta);

        /*
        Generate item lore
         */
        if (!lore.isEmpty())
            ItemTagger.registerCustomLore(itemMeta, lore);

        //Tag the potion effects
        new ElitePotionEffectContainer(itemMeta, potionEffects);

        itemStack.setItemMeta(itemMeta);

        return commonFeatures(itemStack, eliteMobEntity, player, enchantments, customEnchantments, showItemWorth);
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
        HashMap<Enchantment, Integer> enchantmentMap = EnchantmentGenerator.generateEnchantments(itemTier, itemMaterial, itemMeta);

        /*
        Generate custom enchantments
        Note: This only gets a list of enchantments to be applied later at the lore stage
         */
        HashMap<String, Integer> customEnchantmentMap = EnchantmentGenerator.generateCustomEnchantments(itemTier, itemMaterial);

        /*
        Generate item name
         */
        itemMeta.setDisplayName(NameGenerator.generateName(itemMaterial));

        /*
        Colorize with MMO colors
         */
        itemStack.setItemMeta(itemMeta);
        ItemQualityColorizer.dropQualityColorizer(itemStack);

        return commonFeatures(itemStack, killedMob, player, enchantmentMap, customEnchantmentMap, showItemWorth);

    }

    private static ItemStack commonFeatures(ItemStack itemStack,
                                            EliteMobEntity eliteMobEntity,
                                            Player player,
                                            HashMap<Enchantment, Integer> enchantments,
                                            HashMap<String, Integer> customEnchantments,
                                            boolean showItemWorth) {

        ItemMeta itemMeta = itemStack.getItemMeta();

        //hide default lore
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        /*
        Register elite item
         */
        ItemTagger.registerEliteItem(itemMeta);

        /*
        Register item source for lore redraw
         */
        ItemTagger.registerItemSource(eliteMobEntity, itemMeta);

        //Tag the item
        ItemTagger.registerEnchantments(itemMeta, enchantments);
        ItemTagger.registerCustomEnchantments(itemMeta, customEnchantments);

        itemStack.setItemMeta(itemMeta);

        /*
        Add soulbind if applicable
         */
        SoulbindEnchantment.addEnchantment(itemStack, player);

        /*
        Update lore
         */
        new EliteItemLore(itemStack, showItemWorth);

        return itemStack;

    }

}
