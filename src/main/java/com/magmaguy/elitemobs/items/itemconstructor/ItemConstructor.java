package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffectContainer;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ItemConstructor {

    public static ItemStack constructItem(int level,
                                          String rawName,
                                          Material material,
                                          HashMap<Enchantment, Integer> enchantments,
                                          HashMap<String, Integer> customEnchantments,
                                          List<String> potionEffects,
                                          List<String> lore,
                                          EliteEntity eliteEntity,
                                          Player player,
                                          boolean showItemWorth,
                                          int customModelID,
                                          boolean soulbound,
                                          String filename) {
        /*
        Construct initial item
         */
        ItemStack itemStack = ItemStackGenerator.generateItemStack(material);
        /*
        Set the item level
         */
        if (level != -1)
            EliteItemManager.setEliteLevel(itemStack, level);
        /*
        Get meta
         */
        ItemMeta itemMeta = itemStack.getItemMeta();

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
        if (lore != null && !lore.isEmpty())
            ItemTagger.registerCustomLore(itemMeta, lore);

        //Tag the potion effects
        new ElitePotionEffectContainer(itemMeta, potionEffects);

        itemStack.setItemMeta(itemMeta);

        //Apply custom model id
        if (customModelID > 0) {
            itemMeta.setCustomModelData(customModelID);
            itemStack.setItemMeta(itemMeta);
        }


        //Register filename of the custom item into the persistent metadata
        Objects.requireNonNull(itemMeta).getPersistentDataContainer().set(new NamespacedKey(MetadataHandler.PLUGIN, filename), PersistentDataType.STRING, filename);
        itemStack.setItemMeta(itemMeta);

        return commonFeatures(itemStack, eliteEntity, player, enchantments, customEnchantments, showItemWorth, soulbound);
    }

    /*
    For procedurally generated items
     */
    public static ItemStack constructItem(double itemTier, EliteEntity killedMob, Player player, boolean showItemWorth) {

        /*
        Generate material
         */
        Material itemMaterial = MaterialGenerator.generateMaterial(itemTier);
        /*
        Construct initial item
         */
        ItemStack itemStack = ItemStackGenerator.generateItemStack(itemMaterial);
        /*
        Set the item level
         */
        EliteItemManager.setEliteLevel(itemStack, (int) Math.round(itemTier));
        /*
        Get meta
         */
        ItemMeta itemMeta = itemStack.getItemMeta();

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

        return commonFeatures(itemStack, killedMob, player, enchantmentMap, customEnchantmentMap, showItemWorth, true);

    }

    private static ItemStack commonFeatures(ItemStack itemStack,
                                            EliteEntity eliteEntity,
                                            Player player,
                                            HashMap<Enchantment, Integer> enchantments,
                                            HashMap<String, Integer> customEnchantments,
                                            boolean showItemWorth,
                                            boolean soulbound) {

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
        ItemTagger.registerItemSource(eliteEntity, itemMeta);

        //Tag the item
        ItemTagger.registerEnchantments(itemMeta, enchantments);
        ItemTagger.registerCustomEnchantments(itemMeta, customEnchantments);

        itemStack.setItemMeta(itemMeta);

        /*
        Add soulbind if applicable
         */
        if (soulbound) SoulbindEnchantment.addEnchantment(itemStack, player);

        /*
        Update lore
         */
        new EliteItemLore(itemStack, showItemWorth);

        return itemStack;

    }

}
