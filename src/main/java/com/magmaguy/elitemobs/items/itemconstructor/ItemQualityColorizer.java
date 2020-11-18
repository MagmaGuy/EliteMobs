package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemQualityColorizer {

    public enum ItemQuality {
        LIGHT_BLUE,
        GOLD,
        PURPLE,
        BLUE,
        GREEN,
        WHITE,
        GRAY
    }

//    /*
//    item quality: light blue (above max config enchant level) > gold > purple > blue > green > white > gray
//     */
    public static ItemQuality getItemQuality(ItemStack itemStack) {

        ItemMeta itemMeta = itemStack.getItemMeta();

        int enchantmentCount = 0;

        if (!itemMeta.getEnchants().isEmpty()) {

            for (Enchantment enchantment : itemMeta.getEnchants().keySet()) {

                enchantmentCount += itemMeta.getEnchantLevel(enchantment);

            }

        }

        double enchantPercentage = 0;

        //get percentage of max enchants it could have
        if (maxRankCalculator(itemStack) > 0) {
            enchantPercentage = enchantmentCount * 100 / maxRankCalculator(itemStack);
        }

        if (enchantPercentage > 100)
            return ItemQuality.LIGHT_BLUE;
        else if (enchantPercentage > 100 / 6 * 5)
            return ItemQuality.GOLD;
        else if (enchantPercentage > 100 / 6 * 4)
            return ItemQuality.PURPLE;
        else if (enchantPercentage > 100 / 6 * 3)
            return ItemQuality.BLUE;
        else if (enchantPercentage > 100 / 6 * 2)
            return ItemQuality.GREEN;
        else if (enchantPercentage > 100 / 6 * 1)
            return ItemQuality.WHITE;
        else
            return ItemQuality.GRAY;

    }

    public static void dropQualityColorizer(ItemStack itemStack) {

        if (ItemSettingsConfig.doMmorpgColors) {

            ItemMeta itemMeta = itemStack.getItemMeta();

            int enchantmentCount = 0;

            if (!itemMeta.getEnchants().isEmpty()) {

                for (Enchantment enchantment : itemMeta.getEnchants().keySet()) {

                    enchantmentCount += itemMeta.getEnchantLevel(enchantment);

                }

            }

            double enchantPercentage = 0;

            //get percentage of max enchants it could have
            if (maxRankCalculator(itemStack) > 0) {

                enchantPercentage = enchantmentCount * 100 / maxRankCalculator(itemStack);

            }

            if (enchantPercentage > 100) {

                colorizeBoldNameAndLore(ChatColor.DARK_AQUA, itemMeta);

            } else if (enchantPercentage > 100 / 6 * 5) {

                itemMeta = colorizeNameAndLore(ChatColor.GOLD, itemMeta);

            } else if (enchantPercentage > 100 / 6 * 4) {

                itemMeta = colorizeNameAndLore(ChatColor.DARK_PURPLE, itemMeta);

            } else if (enchantPercentage > 100 / 6 * 3) {

                itemMeta = colorizeNameAndLore(ChatColor.BLUE, itemMeta);

            } else if (enchantPercentage > 100 / 6 * 2) {

                itemMeta = colorizeNameAndLore(ChatColor.GREEN, itemMeta);

            } else if (enchantPercentage > 100 / 6 * 1) {

                itemMeta = colorizeNameAndLore(ChatColor.WHITE, itemMeta);

            } else {

                itemMeta = colorizeNameAndLore(ChatColor.GRAY, itemMeta);

            }

            itemStack.setItemMeta(itemMeta);

        }

    }

    private static ItemMeta colorizeNameAndLore(ChatColor chatColor, ItemMeta itemMeta) {

        itemMeta.setDisplayName(chatColor + itemMeta.getDisplayName());

        List list = new ArrayList();

        if (itemMeta.getLore() != null) {

            for (String string : itemMeta.getLore()) {

                if (!string.isEmpty()) {

                    String colorizedString = chatColor + "" + ChatColor.ITALIC + string;
                    list.add(colorizedString);

                }

            }

            itemMeta.setLore(list);

        }

        return itemMeta;

    }

    private static ItemMeta colorizeBoldNameAndLore(ChatColor chatColor, ItemMeta itemMeta) {

        /*
        Cancel colorization in case item already has a color (for custom and unique items)
         */
        if (itemMeta.getDisplayName().equals(ChatColor.stripColor(itemMeta.getDisplayName())))
            itemMeta.setDisplayName(chatColor + "" + ChatColor.BOLD + "" + itemMeta.getDisplayName());

        List list = new ArrayList();

        if (itemMeta.getLore() != null && !itemMeta.getLore().isEmpty()) {

            for (String string : itemMeta.getLore()) {

                if (!string.isEmpty()) {

                    String colorizedString = chatColor + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + string;
                    list.add(colorizedString);

                }

            }

            itemMeta.setLore(list);

        }

        return itemMeta;

    }

    private static int maxRankCalculator(ItemStack itemStack) {

        int maxRank = 0;

        Material material = itemStack.getType();

        switch (material) {
            case DIAMOND_SWORD:
            case GOLDEN_SWORD:
            case IRON_SWORD:
            case STONE_SWORD:
            case WOODEN_SWORD:
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DAMAGE_ARTHROPODS).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.FIRE_ASPECT).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.KNOCKBACK).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.LOOT_BONUS_BLOCKS).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DAMAGE_ALL).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DAMAGE_UNDEAD).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.SWEEPING_EDGE).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DURABILITY).getMaxLevel();
                break;
            case BOW:
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.ARROW_FIRE).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.ARROW_INFINITE).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.ARROW_DAMAGE).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.ARROW_KNOCKBACK).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DURABILITY).getMaxLevel();
                break;
            case DIAMOND_PICKAXE:
            case GOLDEN_PICKAXE:
            case IRON_PICKAXE:
            case STONE_PICKAXE:
            case WOODEN_PICKAXE:
            case DIAMOND_SHOVEL:
            case GOLDEN_SHOVEL:
            case IRON_SHOVEL:
            case STONE_SHOVEL:
            case WOODEN_SHOVEL:
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DIG_SPEED).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.LOOT_BONUS_BLOCKS).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.SILK_TOUCH).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DURABILITY).getMaxLevel();
                break;
            case DIAMOND_HOE:
            case GOLDEN_HOE:
            case IRON_HOE:
            case STONE_HOE:
            case WOODEN_HOE:
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DURABILITY).getMaxLevel();
                break;
            case DIAMOND_AXE:
            case GOLDEN_AXE:
            case IRON_AXE:
            case STONE_AXE:
            case WOODEN_AXE:
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DAMAGE_ARTHROPODS).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DIG_SPEED).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.LOOT_BONUS_BLOCKS).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DAMAGE_ALL).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.SILK_TOUCH).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DAMAGE_UNDEAD).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DURABILITY).getMaxLevel();
                break;
            case CHAINMAIL_HELMET:
            case DIAMOND_HELMET:
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case LEATHER_HELMET:
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DURABILITY).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.OXYGEN).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION_EXPLOSIONS).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION_FIRE).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION_PROJECTILE).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.THORNS).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.WATER_WORKER).getMaxLevel();
                break;
            case CHAINMAIL_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case IRON_CHESTPLATE:
            case LEATHER_CHESTPLATE:
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DURABILITY).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION_EXPLOSIONS).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION_FIRE).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION_PROJECTILE).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.THORNS).getMaxLevel();
                break;

        }


        if (material.equals(Material.CHAINMAIL_LEGGINGS) || material.equals(Material.DIAMOND_LEGGINGS) ||
                material.equals(Material.GOLDEN_LEGGINGS) || material.equals(Material.IRON_LEGGINGS) ||
                material.equals(Material.LEATHER_LEGGINGS)) {

            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DURABILITY).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION_EXPLOSIONS).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION_FIRE).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION_PROJECTILE).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.THORNS).getMaxLevel();

        } else if (material.equals(Material.CHAINMAIL_BOOTS) || material.equals(Material.DIAMOND_BOOTS) ||
                material.equals(Material.GOLDEN_BOOTS) || material.equals(Material.IRON_BOOTS) ||
                material.equals(Material.LEATHER_BOOTS)) {

            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DURABILITY).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION_EXPLOSIONS).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION_FALL).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION_FIRE).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION_PROJECTILE).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.THORNS).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DEPTH_STRIDER).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.FROST_WALKER).getMaxLevel();

        } else if (material.equals(Material.SHEARS)) {

            EnchantmentsConfig.getEnchantment(Enchantment.DIG_SPEED).getMaxLevel();
            EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
            EnchantmentsConfig.getEnchantment(Enchantment.DURABILITY).getMaxLevel();

        } else if (material.equals(Material.FISHING_ROD)) {

            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DURABILITY).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.LUCK).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.LURE).getMaxLevel();

        } else if (material.equals(Material.SHIELD)) {

            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DURABILITY).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();

        }

        return maxRank;

    }

}
