package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.itemconstructor.EliteItemSkins;
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
        ItemStack scythe = new ItemStack(Material.IRON_HOE);
        addDurability(scythe);
        ItemStack bow = new ItemStack(Material.BOW);
        addDurability(bow);
        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        addDurability(crossbow);
        ItemStack cheatSword = new ItemStack(Material.NETHERITE_SWORD);
        addDurability(cheatSword);

        EliteItemManager.setEliteLevel(helmet, tierLevel);
        EliteItemManager.setEliteLevel(chestplate, tierLevel);
        EliteItemManager.setEliteLevel(leggings, tierLevel);
        EliteItemManager.setEliteLevel(boots, tierLevel);
        EliteItemManager.setEliteLevel(sword, tierLevel);
        EliteItemManager.setEliteLevel(axe, tierLevel);
        EliteItemManager.setEliteLevel(scythe, tierLevel);
        EliteItemManager.setEliteLevel(bow, tierLevel);
        EliteItemManager.setEliteLevel(crossbow, tierLevel);
        EliteItemManager.setEliteLevel(cheatSword, tierLevel);
        ItemMeta cheatItemMeta = cheatSword.getItemMeta();
        cheatItemMeta.setDisplayName("CHEAT SWORD");
        ItemTagger.registerEnchantment(cheatItemMeta, Enchantment.SHARPNESS.getKey(), 100);
        cheatSword.setItemMeta(cheatItemMeta);

        new EliteItemLore(helmet, false);
        new EliteItemLore(chestplate, false);
        new EliteItemLore(leggings, false);
        new EliteItemLore(boots, false);
        new EliteItemLore(sword, false);
        new EliteItemLore(axe, false);
        new EliteItemLore(scythe, false);
        new EliteItemLore(bow, false);
        new EliteItemLore(crossbow, false);
        new EliteItemLore(cheatSword, false);

        // Apply level-based custom skins AFTER lore is set
        EliteItemSkins.applyLevelBasedSkin(helmet, tierLevel);
        EliteItemSkins.applyLevelBasedSkin(chestplate, tierLevel);
        EliteItemSkins.applyLevelBasedSkin(leggings, tierLevel);
        EliteItemSkins.applyLevelBasedSkin(boots, tierLevel);
        EliteItemSkins.applyLevelBasedSkin(sword, tierLevel);
        EliteItemSkins.applyLevelBasedSkin(axe, tierLevel);
        EliteItemSkins.applyLevelBasedSkin(scythe, tierLevel);
        EliteItemSkins.applyLevelBasedSkin(bow, tierLevel);
        EliteItemSkins.applyLevelBasedSkin(crossbow, tierLevel);
        // cheatSword intentionally doesn't get custom skin


        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
        player.getInventory().addItem(sword);
        player.getInventory().addItem(axe);
        player.getInventory().addItem(scythe);
        player.getInventory().addItem(bow);
        player.getInventory().addItem(crossbow);
        player.getInventory().addItem(cheatSword);
        player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 64));
        player.getInventory().addItem(new ItemStack(Material.SHIELD));
        player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
        player.getInventory().addItem(new ItemStack(Material.ARROW, 64));

    }

    private static void addDurability(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addEnchant(Enchantment.UNBREAKING, 5, true);
        itemStack.setItemMeta(itemMeta);
    }

}
