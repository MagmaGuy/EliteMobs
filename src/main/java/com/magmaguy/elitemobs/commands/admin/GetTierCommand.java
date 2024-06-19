package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemTagger;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public class GetTierCommand {
    private GetTierCommand() {
    }

    public static void get(Player player, int tierLevel) {

        ItemStack helmet = new ItemStack(Material.IRON_HELMET);
        addDurability(helmet);
        ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
        addDurability(chestplate);
        ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS);
        addDurability(leggings);
        ItemStack boots = new ItemStack(Material.IRON_BOOTS);
        addDurability(boots);
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        addDurability(sword);
        ItemStack axe = new ItemStack(Material.IRON_AXE);
        addDurability(axe);
        ItemStack bow = new ItemStack(Material.BOW);
        addDurability(boots);
        ItemStack cheatSword = new ItemStack(Material.NETHERITE_SWORD);
        addDurability(cheatSword);

        EliteItemManager.setEliteLevel(helmet, tierLevel);
        EliteItemManager.setEliteLevel(chestplate, tierLevel);
        EliteItemManager.setEliteLevel(leggings, tierLevel);
        EliteItemManager.setEliteLevel(boots, tierLevel);
        EliteItemManager.setEliteLevel(sword, tierLevel);
        EliteItemManager.setEliteLevel(axe, tierLevel);
        EliteItemManager.setEliteLevel(bow, tierLevel);
        EliteItemManager.setEliteLevel(cheatSword, tierLevel);
        ItemMeta cheatItemMeta = cheatSword.getItemMeta();
        cheatItemMeta.setDisplayName("CHEAT SWORD");
        ItemTagger.registerEnchantment(cheatItemMeta, Enchantment.SHARPNESS.getKey(), 100);
        cheatSword.setItemMeta(cheatItemMeta);

        new EliteItemLore(helmet, false);
        new EliteItemLore(leggings, false);
        new EliteItemLore(boots, false);
        new EliteItemLore(sword, false);
        new EliteItemLore(axe, false);
        new EliteItemLore(cheatSword, false);
        new EliteItemLore(chestplate, false);
        new EliteItemLore(bow, false);


        player.getInventory().addItem(helmet);
        player.getInventory().addItem(chestplate);
        player.getInventory().addItem(leggings);
        player.getInventory().addItem(boots);
        player.getInventory().addItem(sword);
        player.getInventory().addItem(axe);
        player.getInventory().addItem(bow);
        player.getInventory().addItem(cheatSword);

    }

    private static void addDurability(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        HashMap<Enchantment, Integer> enchantmentIntegerHashMap = new HashMap<>();
        enchantmentIntegerHashMap.put(Enchantment.UNBREAKING, 5);
        ItemTagger.registerEnchantments(itemMeta, enchantmentIntegerHashMap);
        itemStack.setItemMeta(itemMeta);
    }

}
