package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.itemconstructor.EliteItemSkins;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.ArmorSkillHealthBonus;
import com.magmaguy.elitemobs.skills.CombatLevelDisplay;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GetTierCommand {
    private GetTierCommand() {
    }

    public static void get(Player player, int tierLevel) {
        grantLoadout(player, tierLevel, false);
    }

    public static void getLimited(Player player, int tierLevel) {
        grantLoadout(player, tierLevel, true);
    }

    private static void grantLoadout(Player player, int tierLevel, boolean limited) {
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
        ItemStack bow = new ItemStack(Material.BOW);
        addDurability(bow);
        ItemStack spear = null;
        ItemStack axe = null;
        ItemStack scythe = null;
        ItemStack crossbow = null;
        ItemStack trident = null;
        ItemStack mace = null;
        if (!limited) {
            axe = new ItemStack(Material.IRON_AXE);
            addDurability(axe);
            scythe = new ItemStack(Material.IRON_HOE);
            addDurability(scythe);
            crossbow = new ItemStack(Material.CROSSBOW);
            addDurability(crossbow);
            trident = new ItemStack(Material.TRIDENT);
            addDurability(trident);
            mace = new ItemStack(Material.MACE);
            addDurability(mace);
            try {
                spear = new ItemStack(Material.IRON_SPEAR);
                addDurability(spear);
            } catch (NoSuchFieldError ignored) {
                // SPEAR doesn't exist pre-1.21.11
            }
        }
        ItemStack cheatSword = new ItemStack(Material.NETHERITE_SWORD);
        addDurability(cheatSword);

        EliteItemManager.setEliteLevel(helmet, tierLevel);
        EliteItemManager.setEliteLevel(chestplate, tierLevel);
        EliteItemManager.setEliteLevel(leggings, tierLevel);
        EliteItemManager.setEliteLevel(boots, tierLevel);
        EliteItemManager.setEliteLevel(sword, tierLevel);
        EliteItemManager.setEliteLevel(bow, tierLevel);
        if (axe != null) EliteItemManager.setEliteLevel(axe, tierLevel);
        if (scythe != null) EliteItemManager.setEliteLevel(scythe, tierLevel);
        if (crossbow != null) EliteItemManager.setEliteLevel(crossbow, tierLevel);
        if (trident != null) EliteItemManager.setEliteLevel(trident, tierLevel);
        if (mace != null) EliteItemManager.setEliteLevel(mace, tierLevel);
        if (spear != null) EliteItemManager.setEliteLevel(spear, tierLevel);
        EliteItemManager.setEliteLevel(cheatSword, tierLevel);
        ItemMeta cheatItemMeta = cheatSword.getItemMeta();
        cheatItemMeta.setDisplayName(CommandMessagesConfig.getCheatSwordDisplayName());
        ItemTagger.registerEnchantment(cheatItemMeta, Enchantment.SHARPNESS.getKey(), 100);
        cheatSword.setItemMeta(cheatItemMeta);

        new EliteItemLore(helmet, false);
        new EliteItemLore(chestplate, false);
        new EliteItemLore(leggings, false);
        new EliteItemLore(boots, false);
        new EliteItemLore(sword, false);
        new EliteItemLore(bow, false);
        if (axe != null) new EliteItemLore(axe, false);
        if (scythe != null) new EliteItemLore(scythe, false);
        if (crossbow != null) new EliteItemLore(crossbow, false);
        if (trident != null) new EliteItemLore(trident, false);
        if (mace != null) new EliteItemLore(mace, false);
        if (spear != null) new EliteItemLore(spear, false);
        new EliteItemLore(cheatSword, false);

        // Apply level-based custom skins AFTER lore is set
        EliteItemSkins.applyLevelBasedSkin(helmet, tierLevel);
        EliteItemSkins.applyLevelBasedSkin(chestplate, tierLevel);
        EliteItemSkins.applyLevelBasedSkin(leggings, tierLevel);
        EliteItemSkins.applyLevelBasedSkin(boots, tierLevel);
        EliteItemSkins.applyLevelBasedSkin(sword, tierLevel);
        EliteItemSkins.applyLevelBasedSkin(bow, tierLevel);
        if (axe != null) EliteItemSkins.applyLevelBasedSkin(axe, tierLevel);
        if (scythe != null) EliteItemSkins.applyLevelBasedSkin(scythe, tierLevel);
        if (crossbow != null) EliteItemSkins.applyLevelBasedSkin(crossbow, tierLevel);
        if (trident != null) EliteItemSkins.applyLevelBasedSkin(trident, tierLevel);
        if (mace != null) EliteItemSkins.applyLevelBasedSkin(mace, tierLevel);
        if (spear != null) EliteItemSkins.applyLevelBasedSkin(spear, tierLevel);
        // cheatSword intentionally doesn't get custom skin


        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
        player.getInventory().addItem(sword);
        player.getInventory().addItem(bow);
        if (axe != null) player.getInventory().addItem(axe);
        if (scythe != null) player.getInventory().addItem(scythe);
        if (crossbow != null) player.getInventory().addItem(crossbow);
        if (trident != null) player.getInventory().addItem(trident);
        if (mace != null) player.getInventory().addItem(mace);
        if (spear != null) player.getInventory().addItem(spear);
        player.getInventory().addItem(cheatSword);
        player.getInventory().addItem(new ItemStack(Material.SHIELD));
        player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
        if (!limited) player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 64));
        if (!limited) player.getInventory().addItem(new ItemStack(Material.ARROW, 64));

        // Also set all skill levels to match the tier level
        long targetXP = SkillXPCalculator.totalXPForLevel(tierLevel);
        for (SkillType skillType : SkillType.values()) {
            PlayerData.setSkillXP(player.getUniqueId(), skillType, targetXP);
        }

        // Update combat level display
        CombatLevelDisplay.updateDisplay(player);

        // Update armor health bonus (since armor skill was changed)
        ArmorSkillHealthBonus.updateHealthBonus(player);

        Logger.sendMessage(player, CommandMessagesConfig.getGetTierGaveGearMessage().replace("$level", String.valueOf(tierLevel)));
        Logger.sendMessage(player, CommandMessagesConfig.getGetTierIronSwordMessage());
        Logger.sendMessage(player, CommandMessagesConfig.getGetTierCheatSwordMessage());
    }

    private static void addDurability(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addEnchant(Enchantment.UNBREAKING, 5, true);
        itemStack.setItemMeta(itemMeta);
    }

}
