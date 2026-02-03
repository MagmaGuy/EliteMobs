package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemQualityColorizer {

    // Gradient color definitions for item quality tiers
    // LIGHT_BLUE (Mythic, >100%) - Prismatic shifting cyan-magenta-cyan
    private static final String MYTHIC_GRADIENT = "<g:#00FFFF:#FF00FF:#00FFFF>";
    private static final String MYTHIC_GRADIENT_END = "</g>";
    // GOLD (Legendary, >83%) - Rich gold-orange-gold gradient
    private static final String LEGENDARY_GRADIENT = "<g:#FFD700:#FFA500:#FFD700>";
    private static final String LEGENDARY_GRADIENT_END = "</g>";
    // PURPLE (Epic, >67%) - Deep purple gradient
    private static final String EPIC_GRADIENT = "<g:#9400D3:#8A2BE2>";
    private static final String EPIC_GRADIENT_END = "</g>";
    // BLUE (Rare, >50%) - Blue gradient
    private static final String RARE_GRADIENT = "<g:#4169E1:#1E90FF>";
    private static final String RARE_GRADIENT_END = "</g>";

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
        else if (enchantPercentage > 100 / 6)
            return ItemQuality.WHITE;
        else
            return ItemQuality.GRAY;

    }

    public static void dropQualityColorizer(ItemStack itemStack) {

        if (ItemSettingsConfig.isDoMmorpgColors()) {

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

                // Mythic tier - prismatic gradient with bold
                itemMeta = colorizeGradientBoldNameAndLore(MYTHIC_GRADIENT, MYTHIC_GRADIENT_END, itemMeta);

            } else if (enchantPercentage > 100 / 6 * 5) {

                // Legendary tier - gold gradient
                itemMeta = colorizeGradientNameAndLore(LEGENDARY_GRADIENT, LEGENDARY_GRADIENT_END, itemMeta);

            } else if (enchantPercentage > 100 / 6 * 4) {

                // Epic tier - purple gradient
                itemMeta = colorizeGradientNameAndLore(EPIC_GRADIENT, EPIC_GRADIENT_END, itemMeta);

            } else if (enchantPercentage > 100 / 6 * 3) {

                // Rare tier - blue gradient
                itemMeta = colorizeGradientNameAndLore(RARE_GRADIENT, RARE_GRADIENT_END, itemMeta);

            } else if (enchantPercentage > 100 / 6 * 2) {

                itemMeta = colorizeNameAndLore(ChatColor.GREEN, itemMeta);

            } else if (enchantPercentage > 100 / 6) {

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

    private static ItemMeta colorizeGradientNameAndLore(String gradientStart, String gradientEnd, ItemMeta itemMeta) {

        /*
        Cancel colorization in case item already has a color (for custom and unique items)
         */
        if (itemMeta.getDisplayName().equals(ChatColor.stripColor(itemMeta.getDisplayName()))) {
            String gradientName = gradientStart + itemMeta.getDisplayName() + gradientEnd;
            itemMeta.setDisplayName(ChatColorConverter.convert(gradientName));
        }

        List<String> list = new ArrayList<>();

        if (itemMeta.getLore() != null) {

            for (String string : itemMeta.getLore()) {

                if (!string.isEmpty()) {

                    String gradientLore = gradientStart + string + gradientEnd;
                    String colorizedString = ChatColorConverter.convert(gradientLore) + ChatColor.ITALIC;
                    list.add(colorizedString);

                }

            }

            itemMeta.setLore(list);

        }

        return itemMeta;

    }

    private static ItemMeta colorizeGradientBoldNameAndLore(String gradientStart, String gradientEnd, ItemMeta itemMeta) {

        /*
        Cancel colorization in case item already has a color (for custom and unique items)
         */
        if (itemMeta.getDisplayName().equals(ChatColor.stripColor(itemMeta.getDisplayName()))) {
            String gradientName = gradientStart + itemMeta.getDisplayName() + gradientEnd;
            itemMeta.setDisplayName(ChatColor.BOLD + ChatColorConverter.convert(gradientName));
        }

        List<String> list = new ArrayList<>();

        if (itemMeta.getLore() != null && !itemMeta.getLore().isEmpty()) {

            for (String string : itemMeta.getLore()) {

                if (!string.isEmpty()) {

                    String gradientLore = gradientStart + string + gradientEnd;
                    String colorizedString = ChatColor.BOLD + "" + ChatColor.ITALIC + ChatColorConverter.convert(gradientLore);
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
            itemMeta.setDisplayName(chatColor + "" + ChatColor.BOLD + itemMeta.getDisplayName());

        List list = new ArrayList();

        if (itemMeta.getLore() != null && !itemMeta.getLore().isEmpty()) {

            for (String string : itemMeta.getLore()) {

                if (!string.isEmpty()) {

                    String colorizedString = chatColor + "" + ChatColor.BOLD + ChatColor.ITALIC + string;
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
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.BANE_OF_ARTHROPODS).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.FIRE_ASPECT).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.KNOCKBACK).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.FORTUNE).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.SHARPNESS).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.SMITE).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.SWEEPING_EDGE).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.UNBREAKING).getMaxLevel();
                break;
            case BOW:
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.FLAME).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.INFINITY).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.POWER).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PUNCH).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.UNBREAKING).getMaxLevel();
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
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.EFFICIENCY).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.FORTUNE).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.SILK_TOUCH).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.UNBREAKING).getMaxLevel();
                break;
            case DIAMOND_HOE:
            case GOLDEN_HOE:
            case IRON_HOE:
            case STONE_HOE:
            case WOODEN_HOE:
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.UNBREAKING).getMaxLevel();
                break;
            case DIAMOND_AXE:
            case GOLDEN_AXE:
            case IRON_AXE:
            case STONE_AXE:
            case WOODEN_AXE:
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.BANE_OF_ARTHROPODS).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.EFFICIENCY).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.FORTUNE).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.SHARPNESS).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.SILK_TOUCH).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.SMITE).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.UNBREAKING).getMaxLevel();
                break;
            case CHAINMAIL_HELMET:
            case DIAMOND_HELMET:
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case LEATHER_HELMET:
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.UNBREAKING).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.RESPIRATION).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.BLAST_PROTECTION).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.FIRE_PROTECTION).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROJECTILE_PROTECTION).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.THORNS).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.AQUA_AFFINITY).getMaxLevel();
                break;
            case CHAINMAIL_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case IRON_CHESTPLATE:
            case LEATHER_CHESTPLATE:
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.UNBREAKING).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.BLAST_PROTECTION).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.FIRE_PROTECTION).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROJECTILE_PROTECTION).getMaxLevel();
                maxRank += EnchantmentsConfig.getEnchantment(Enchantment.THORNS).getMaxLevel();
                break;

        }


        if (material.equals(Material.CHAINMAIL_LEGGINGS) || material.equals(Material.DIAMOND_LEGGINGS) ||
                material.equals(Material.GOLDEN_LEGGINGS) || material.equals(Material.IRON_LEGGINGS) ||
                material.equals(Material.LEATHER_LEGGINGS)) {

            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.UNBREAKING).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.BLAST_PROTECTION).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.FIRE_PROTECTION).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROJECTILE_PROTECTION).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.THORNS).getMaxLevel();

        } else if (material.equals(Material.CHAINMAIL_BOOTS) || material.equals(Material.DIAMOND_BOOTS) ||
                material.equals(Material.GOLDEN_BOOTS) || material.equals(Material.IRON_BOOTS) ||
                material.equals(Material.LEATHER_BOOTS)) {

            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.UNBREAKING).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.BLAST_PROTECTION).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.FEATHER_FALLING).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.FIRE_PROTECTION).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.PROJECTILE_PROTECTION).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.THORNS).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.DEPTH_STRIDER).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.FROST_WALKER).getMaxLevel();

        } else if (material.equals(Material.SHEARS)) {

            EnchantmentsConfig.getEnchantment(Enchantment.EFFICIENCY).getMaxLevel();
            EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
            EnchantmentsConfig.getEnchantment(Enchantment.UNBREAKING).getMaxLevel();

        } else if (material.equals(Material.FISHING_ROD)) {

            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.UNBREAKING).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.LUCK_OF_THE_SEA).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.LURE).getMaxLevel();

        } else if (material.equals(Material.SHIELD)) {

            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.UNBREAKING).getMaxLevel();
            maxRank += EnchantmentsConfig.getEnchantment(Enchantment.MENDING).getMaxLevel();

        }

        return maxRank;

    }

    public enum ItemQuality {
        LIGHT_BLUE,
        GOLD,
        PURPLE,
        BLUE,
        GREEN,
        WHITE,
        GRAY
    }

}
