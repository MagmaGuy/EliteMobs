package com.magmaguy.elitemobs.commands.admin;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GetTierCommand {

    public static void get(Player player, int tierLevel) {

        ItemStack helmet = new ItemStack(Material.NETHERITE_HELMET);
        ItemStack chestplate = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.NETHERITE_LEGGINGS);
        ItemStack boots = new ItemStack(Material.NETHERITE_BOOTS);
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemStack axe = new ItemStack(Material.NETHERITE_AXE);
        ItemStack bow = new ItemStack(Material.BOW);

        if (tierLevel > 8) {

            applyEnchantment(helmet, Enchantment.PROTECTION_ENVIRONMENTAL, tierLevel - 8);
            applyEnchantment(chestplate, Enchantment.PROTECTION_ENVIRONMENTAL, tierLevel - 8);
            applyEnchantment(leggings, Enchantment.PROTECTION_ENVIRONMENTAL, tierLevel - 8);
            applyEnchantment(boots, Enchantment.PROTECTION_ENVIRONMENTAL, tierLevel - 8);
            applyEnchantment(sword, Enchantment.DAMAGE_ALL, tierLevel - 8);
            applyEnchantment(axe, Enchantment.DAMAGE_ALL, tierLevel - 8);
            applyEnchantment(bow, Enchantment.ARROW_DAMAGE, tierLevel - 8);

        }

        player.getInventory().addItem(helmet);
        player.getInventory().addItem(chestplate);
        player.getInventory().addItem(leggings);
        player.getInventory().addItem(boots);
        player.getInventory().addItem(sword);
        player.getInventory().addItem(axe);
        player.getInventory().addItem(bow);

    }

    private static void applyEnchantment(ItemStack itemStack, Enchantment enchantment, int tierLevel) {

        ItemMeta newItemMeta = itemStack.getItemMeta();
        newItemMeta.addEnchant(enchantment, tierLevel - 1, true);
        itemStack.setItemMeta(newItemMeta);

    }

}
