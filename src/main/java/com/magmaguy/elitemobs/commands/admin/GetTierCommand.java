package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.items.ItemTagger;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        ItemTagger.registerEnchantment(cheatItemMeta, Enchantment.DAMAGE_ALL.getKey(), 100);
        cheatSword.setItemMeta(cheatItemMeta);

        player.getInventory().addItem(helmet);
        player.getInventory().addItem(chestplate);
        player.getInventory().addItem(leggings);
        player.getInventory().addItem(boots);
        player.getInventory().addItem(sword);
        player.getInventory().addItem(axe);
        player.getInventory().addItem(bow);
        player.getInventory().addItem(cheatSword);

    }

    private static void addDurability(ItemStack itemStack){
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addEnchant(Enchantment.DURABILITY, 5, false);
        itemStack.setItemMeta(itemMeta);
    }

}
