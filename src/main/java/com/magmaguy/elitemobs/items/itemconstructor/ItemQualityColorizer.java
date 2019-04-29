/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.ItemsProceduralSettingsConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
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

    /*
    item quality: light blue (above max config enchant level) > gold > purple > blue > green > white > gray
     */
    private static final int ARROW_DAMAGE = enchantMaxValueGetter(ItemsProceduralSettingsConfig.ARROW_DAMAGE_BOOL, ItemsProceduralSettingsConfig.ARROW_DAMAGE_MAX_LEVEL);
    private static final int ARROW_FIRE = enchantMaxValueGetter(ItemsProceduralSettingsConfig.ARROW_FIRE_BOOL, ItemsProceduralSettingsConfig.ARROW_FIRE_MAX_LEVEL);
    private static final int ARROW_INFINITE = enchantMaxValueGetter(ItemsProceduralSettingsConfig.ARROW_INFINITE_BOOL);
    private static final int ARROW_KNOCKBACK = enchantMaxValueGetter(ItemsProceduralSettingsConfig.ARROW_KNOCKBACK_BOOL, ItemsProceduralSettingsConfig.ARROW_KNOCKBACK_MAX_LEVEL);
    private static final int BINDING_CURSE = enchantMaxValueGetter(ItemsProceduralSettingsConfig.BINDING_CURSE_BOOL);
    private static final int DAMAGE_ALL = enchantMaxValueGetter(ItemsProceduralSettingsConfig.DAMAGE_ALL_BOOL, ItemsProceduralSettingsConfig.DAMAGE_ALL_MAX_LEVEL);
    private static final int DAMAGE_ARTHROPODS = enchantMaxValueGetter(ItemsProceduralSettingsConfig.DAMAGE_ARTHROPODS_BOOL, ItemsProceduralSettingsConfig.DAMAGE_ARTHROPODS_MAX_LEVEL);
    private static final int DAMAGE_UNDEAD = enchantMaxValueGetter(ItemsProceduralSettingsConfig.DAMAGE_UNDEAD_BOOL, ItemsProceduralSettingsConfig.DAMAGE_UNDEAD_MAX_LEVEL);
    private static final int DEPTH_STRIDER = enchantMaxValueGetter(ItemsProceduralSettingsConfig.DEPTH_STRIDER_BOOL, ItemsProceduralSettingsConfig.DEPTH_STRIDER_MAX_LEVEL);
    private static final int DIG_SPEED = enchantMaxValueGetter(ItemsProceduralSettingsConfig.DIG_SPEED_BOOL, ItemsProceduralSettingsConfig.DIG_SPEED_MAX_LEVEL);
    private static final int DURABILITY = enchantMaxValueGetter(ItemsProceduralSettingsConfig.DURABILITY_BOOL, ItemsProceduralSettingsConfig.DURABILITY_MAX_LEVEL);
    private static final int FIRE_ASPECT = enchantMaxValueGetter(ItemsProceduralSettingsConfig.FIRE_ASPECT_BOOL, ItemsProceduralSettingsConfig.FIRE_ASPECT_MAX_LEVEL);
    private static final int FROST_WALKER = enchantMaxValueGetter(ItemsProceduralSettingsConfig.FROST_WALKER_BOOL, ItemsProceduralSettingsConfig.FROST_WALKER_MAX_LEVEL);
    private static final int KNOCKBACK = enchantMaxValueGetter(ItemsProceduralSettingsConfig.KNOCKBACK_BOOL, ItemsProceduralSettingsConfig.KNOCKBACK_MAX_LEVEL);
    private static final int LOOT_BONUS_BLOCKS = enchantMaxValueGetter(ItemsProceduralSettingsConfig.LOOT_BONUS_BLOCKS_BOOL, ItemsProceduralSettingsConfig.LOOT_BONUS_BLOCKS_MAX_LEVEL);
    private static final int LOOT_BONUS_MOBS = enchantMaxValueGetter(ItemsProceduralSettingsConfig.LOOT_BONUS_MOBS_BOOL, ItemsProceduralSettingsConfig.LOOT_BONUS_MOBS_MAX_LEVEL);
    private static final int LUCK = enchantMaxValueGetter(ItemsProceduralSettingsConfig.LUCK_BOOL, ItemsProceduralSettingsConfig.LUCK_MAX_LEVEL);
    private static final int LURE = enchantMaxValueGetter(ItemsProceduralSettingsConfig.LURE_BOOL, ItemsProceduralSettingsConfig.LURE_MAX_LEVEL);
    private static final int MENDING = enchantMaxValueGetter(ItemsProceduralSettingsConfig.MENDING_BOOL);
    private static final int OXYGEN = enchantMaxValueGetter(ItemsProceduralSettingsConfig.OXYGEN_BOOL, ItemsProceduralSettingsConfig.OXYGEN_MAX_LEVEL);
    private static final int PROTECTION_ENVIRONMENTAL = enchantMaxValueGetter(ItemsProceduralSettingsConfig.PROTECTION_ENVIRONMENTAL_BOOL, ItemsProceduralSettingsConfig.PROTECTION_ENVIRONMENTAL_MAX_LEVEL);
    private static final int PROTECTION_EXPLOSIONS = enchantMaxValueGetter(ItemsProceduralSettingsConfig.PROTECTION_EXPLOSIONS_BOOL, ItemsProceduralSettingsConfig.PROTECTION_EXPLOSIONS_MAX_LEVEL);
    private static final int PROTECTION_FALL = enchantMaxValueGetter(ItemsProceduralSettingsConfig.PROTECTION_FALL_BOOL, ItemsProceduralSettingsConfig.PROTECTION_FALL_MAX_LEVEL);
    private static final int PROTECTION_FIRE = enchantMaxValueGetter(ItemsProceduralSettingsConfig.PROTECTION_FIRE_BOOL, ItemsProceduralSettingsConfig.PROTECTION_FIRE_MAX_LEVEL);
    private static final int PROTECTION_PROJECTILE = enchantMaxValueGetter(ItemsProceduralSettingsConfig.PROTECTION_PROJECTILE_BOOL, ItemsProceduralSettingsConfig.PROTECTION_PROJECTILE_MAX_LEVEL);
    private static final int SILK_TOUCH = enchantMaxValueGetter(ItemsProceduralSettingsConfig.SILK_TOUCH_BOOL);
    private static final int SWEEPING_EDGE = enchantMaxValueGetter(ItemsProceduralSettingsConfig.SWEEPING_EDGE_BOOL, ItemsProceduralSettingsConfig.SWEEPING_EDGE_MAX_LEVEL);
    private static final int THORNS = enchantMaxValueGetter(ItemsProceduralSettingsConfig.THORNS_BOOL, ItemsProceduralSettingsConfig.THORNS_MAX_LEVEL);
    private static final int VANISHING_CURSE = enchantMaxValueGetter(ItemsProceduralSettingsConfig.VANISHING_CURSE_BOOL);
    private static final int WATER_WORKER = enchantMaxValueGetter(ItemsProceduralSettingsConfig.WATER_WORKER_BOOL, ItemsProceduralSettingsConfig.WATER_WORKER_MAX_LEVEL);

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

        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.MMORPG_COLORS)) {

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

        if (!itemMeta.getLore().isEmpty()) {

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

        if (material.equals(Material.DIAMOND_SWORD) || material.equals(Material.GOLDEN_SWORD) ||
                material.equals(Material.IRON_SWORD) || material.equals(Material.STONE_SWORD) ||
                material.equals(Material.WOODEN_SWORD)) {

            maxRank += DAMAGE_ARTHROPODS;
            maxRank += VANISHING_CURSE;
            maxRank += FIRE_ASPECT;
            maxRank += KNOCKBACK;
            maxRank += LOOT_BONUS_MOBS;
            maxRank += MENDING;
            maxRank += DAMAGE_ALL;
            maxRank += DAMAGE_UNDEAD;
            maxRank += SWEEPING_EDGE;
            maxRank += DURABILITY;

        } else if (material.equals(Material.BOW)) {

            maxRank += VANISHING_CURSE;
            maxRank += ARROW_FIRE;
            maxRank += ARROW_INFINITE;
            maxRank += MENDING;
            maxRank += ARROW_DAMAGE;
            maxRank += ARROW_KNOCKBACK;
            maxRank += DURABILITY;

        } else if (material.equals(Material.DIAMOND_PICKAXE) || material.equals(Material.GOLDEN_SWORD) ||
                material.equals(Material.IRON_PICKAXE) || material.equals(Material.STONE_PICKAXE) ||
                material.equals(Material.WOODEN_SWORD)) {

            maxRank += VANISHING_CURSE;
            maxRank += DIG_SPEED;
            maxRank += LOOT_BONUS_BLOCKS;
            maxRank += MENDING;
            maxRank += SILK_TOUCH;
            maxRank += DURABILITY;

        } else if (material.equals(Material.DIAMOND_SHOVEL) || material.equals(Material.GOLDEN_SHOVEL) ||
                material.equals(Material.IRON_SHOVEL) || material.equals(Material.STONE_SHOVEL) ||
                material.equals(Material.WOODEN_SHOVEL)) {

            maxRank += VANISHING_CURSE;
            maxRank += DIG_SPEED;
            maxRank += LOOT_BONUS_BLOCKS;
            maxRank += MENDING;
            maxRank += SILK_TOUCH;
            maxRank += DURABILITY;

        } else if (material.equals(Material.DIAMOND_HOE) || material.equals(Material.GOLDEN_HOE) ||
                material.equals(Material.IRON_HOE) || material.equals(Material.STONE_HOE) ||
                material.equals(Material.WOODEN_HOE)) {

            maxRank += VANISHING_CURSE;
            maxRank += MENDING;
            maxRank += DURABILITY;

        } else if (material.equals(Material.DIAMOND_AXE) || material.equals(Material.GOLDEN_AXE) ||
                material.equals(Material.IRON_AXE) || material.equals(Material.STONE_AXE) ||
                material.equals(Material.WOODEN_AXE)) {

            maxRank += DAMAGE_ARTHROPODS;
            maxRank += VANISHING_CURSE;
            maxRank += DIG_SPEED;
            maxRank += LOOT_BONUS_BLOCKS;
            maxRank += MENDING;
            maxRank += DAMAGE_ALL;
            maxRank += SILK_TOUCH;
            maxRank += DAMAGE_UNDEAD;
            maxRank += DURABILITY;

        } else if (material.equals(Material.CHAINMAIL_HELMET) || material.equals(Material.DIAMOND_HELMET) ||
                material.equals(Material.GOLDEN_HELMET) || material.equals(Material.IRON_HELMET) ||
                material.equals(Material.LEATHER_HELMET)) {

            maxRank += BINDING_CURSE;
            maxRank += DURABILITY;
            maxRank += MENDING;
            maxRank += OXYGEN;
            maxRank += PROTECTION_ENVIRONMENTAL;
            maxRank += PROTECTION_EXPLOSIONS;
            maxRank += PROTECTION_FIRE;
            maxRank += PROTECTION_PROJECTILE;
            maxRank += THORNS;
            maxRank += VANISHING_CURSE;
            maxRank += WATER_WORKER;

        } else if (material.equals(Material.CHAINMAIL_CHESTPLATE) || material.equals(Material.DIAMOND_CHESTPLATE) ||
                material.equals(Material.GOLDEN_CHESTPLATE) || material.equals(Material.IRON_CHESTPLATE) ||
                material.equals(Material.LEATHER_CHESTPLATE)) {

            maxRank += BINDING_CURSE;
            maxRank += DURABILITY;
            maxRank += MENDING;
            maxRank += PROTECTION_ENVIRONMENTAL;
            maxRank += PROTECTION_EXPLOSIONS;
            maxRank += PROTECTION_FIRE;
            maxRank += PROTECTION_PROJECTILE;
            maxRank += THORNS;
            maxRank += VANISHING_CURSE;

        } else if (material.equals(Material.CHAINMAIL_LEGGINGS) || material.equals(Material.DIAMOND_LEGGINGS) ||
                material.equals(Material.GOLDEN_LEGGINGS) || material.equals(Material.IRON_LEGGINGS) ||
                material.equals(Material.LEATHER_LEGGINGS)) {

            maxRank += BINDING_CURSE;
            maxRank += DURABILITY;
            maxRank += MENDING;
            maxRank += PROTECTION_ENVIRONMENTAL;
            maxRank += PROTECTION_EXPLOSIONS;
            maxRank += PROTECTION_FIRE;
            maxRank += PROTECTION_PROJECTILE;
            maxRank += THORNS;
            maxRank += VANISHING_CURSE;

        } else if (material.equals(Material.CHAINMAIL_BOOTS) || material.equals(Material.DIAMOND_BOOTS) ||
                material.equals(Material.GOLDEN_BOOTS) || material.equals(Material.IRON_BOOTS) ||
                material.equals(Material.LEATHER_BOOTS)) {

            maxRank += BINDING_CURSE;
            maxRank += DURABILITY;
            maxRank += MENDING;
            maxRank += PROTECTION_ENVIRONMENTAL;
            maxRank += PROTECTION_EXPLOSIONS;
            maxRank += PROTECTION_FALL;
            maxRank += PROTECTION_FIRE;
            maxRank += PROTECTION_PROJECTILE;
            maxRank += THORNS;
            maxRank += VANISHING_CURSE;
            maxRank += DEPTH_STRIDER;
            maxRank += FROST_WALKER;

        } else if (material.equals(Material.SHEARS)) {

            maxRank += VANISHING_CURSE;
            maxRank += DIG_SPEED;
            maxRank += MENDING;
            maxRank += DURABILITY;

        } else if (material.equals(Material.FISHING_ROD)) {

            maxRank += VANISHING_CURSE;
            maxRank += DURABILITY;
            maxRank += MENDING;
            maxRank += LUCK;
            maxRank += LURE;

        } else if (material.equals(Material.SHIELD)) {

            maxRank += VANISHING_CURSE;
            maxRank += DURABILITY;
            maxRank += MENDING;

        }

        return maxRank;

    }

    private static int enchantMaxValueGetter(String boolString) {

        Configuration configuration = ConfigValues.itemsProceduralSettingsConfig;

        if (configuration.getBoolean(boolString)) {

            return 1;

        }

        return 0;

    }

    private static int enchantMaxValueGetter(String boolString, String maxLevelString) {

        Configuration configuration = ConfigValues.itemsProceduralSettingsConfig;

        if (configuration.getBoolean(boolString)) {

            return configuration.getInt(maxLevelString);

        }

        return 0;

    }

}
