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

package com.magmaguy.elitemobs.elitedrops;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.RandomItemsSettingsConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DropQuality {

    //item quality: light blue (above max config enchant level) > gold > purple > blue > green > white > gray
    //calculate item quality percentually from the max item level set in configs
    //6 ranks so 100/6 = 16,67% of theoretical max per rank

    public static void dropQualityColorizer(ItemStack itemStack) {

        if (ConfigValues.defaultConfig.getBoolean("Use MMORPG colors for item ranks")) {

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

                itemMeta.setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + itemMeta.getDisplayName());

                List list = new ArrayList();

                for (String string : itemMeta.getLore()) {

                    String coloredString = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + string;
                    list.add(coloredString);

                }

                itemMeta.setLore(list);

            } else if (enchantPercentage > 100 / 6 * 5) {

                itemMeta.setDisplayName(ChatColor.GOLD + itemMeta.getDisplayName());

                List list = new ArrayList();

                for (String string : itemMeta.getLore()) {

                    String coloredString = ChatColor.GOLD + "" + ChatColor.ITALIC + string;
                    list.add(coloredString);

                }

                itemMeta.setLore(list);

            } else if (enchantPercentage > 100 / 6 * 4) {

                itemMeta.setDisplayName(ChatColor.DARK_PURPLE + itemMeta.getDisplayName());

                List list = new ArrayList();

                for (String string : itemMeta.getLore()) {

                    String coloredString = ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + string;
                    list.add(coloredString);

                }

                itemMeta.setLore(list);

            } else if (enchantPercentage > 100 / 6 * 3) {

                itemMeta.setDisplayName(ChatColor.BLUE + itemMeta.getDisplayName());

                List list = new ArrayList();

                for (String string : itemMeta.getLore()) {

                    String coloredString = ChatColor.BLUE + "" + ChatColor.ITALIC + string;
                    list.add(coloredString);

                }

                itemMeta.setLore(list);

            } else if (enchantPercentage > 100 / 6 * 2) {

                itemMeta.setDisplayName(ChatColor.GREEN + itemMeta.getDisplayName());

                List list = new ArrayList();

                for (String string : itemMeta.getLore()) {

                    String coloredString = ChatColor.GREEN + "" + ChatColor.ITALIC + string;
                    list.add(coloredString);

                }

                itemMeta.setLore(list);

            } else if (enchantPercentage > 100 / 6 * 1) {

                itemMeta.setDisplayName(ChatColor.WHITE + itemMeta.getDisplayName());

                List list = new ArrayList();

                for (String string : itemMeta.getLore()) {

                    String coloredString = ChatColor.WHITE + "" + ChatColor.ITALIC + string;
                    list.add(coloredString);

                }

                itemMeta.setLore(list);

            } else {

                itemMeta.setDisplayName(ChatColor.GRAY + itemMeta.getDisplayName());

                List list = new ArrayList();

                for (String string : itemMeta.getLore()) {

                    String coloredString = ChatColor.GRAY + "" + ChatColor.ITALIC + string;
                    list.add(coloredString);

                }

                itemMeta.setLore(list);

            }

            itemStack.setItemMeta(itemMeta);

        }

    }

    private static int maxRankCalculator(ItemStack itemStack) {

        Configuration configuration = ConfigValues.randomItemsConfig;

        int maxRank = 0;

        Material material = itemStack.getType();

        if (material.equals(Material.DIAMOND_SWORD) || material.equals(Material.GOLD_SWORD) ||
                material.equals(Material.IRON_SWORD) || material.equals(Material.STONE_SWORD) ||
                material.equals(Material.WOOD_SWORD)) {

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

        } else if (material.equals(Material.DIAMOND_PICKAXE) || material.equals(Material.GOLD_PICKAXE) ||
                material.equals(Material.IRON_PICKAXE) || material.equals(Material.STONE_PICKAXE) ||
                material.equals(Material.WOOD_PICKAXE)) {

            maxRank += VANISHING_CURSE;
            maxRank += DIG_SPEED;
            maxRank += LOOT_BONUS_BLOCKS;
            maxRank += MENDING;
            maxRank += SILK_TOUCH;
            maxRank += DURABILITY;

        } else if (material.equals(Material.DIAMOND_SPADE) || material.equals(Material.GOLD_SPADE) ||
                material.equals(Material.IRON_SPADE) || material.equals(Material.STONE_SPADE) ||
                material.equals(Material.WOOD_SPADE)) {

            maxRank += VANISHING_CURSE;
            maxRank += DIG_SPEED;
            maxRank += LOOT_BONUS_BLOCKS;
            maxRank += MENDING;
            maxRank += SILK_TOUCH;
            maxRank += DURABILITY;

        } else if (material.equals(Material.DIAMOND_HOE) || material.equals(Material.GOLD_HOE) ||
                material.equals(Material.IRON_HOE) || material.equals(Material.STONE_HOE) ||
                material.equals(Material.WOOD_HOE)) {

            maxRank += VANISHING_CURSE;
            maxRank += MENDING;
            maxRank += DURABILITY;

        } else if (material.equals(Material.DIAMOND_AXE) || material.equals(Material.GOLD_AXE) ||
                material.equals(Material.IRON_AXE) || material.equals(Material.STONE_AXE) ||
                material.equals(Material.WOOD_AXE)) {

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
                material.equals(Material.GOLD_HELMET) || material.equals(Material.IRON_HELMET) ||
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
                material.equals(Material.GOLD_CHESTPLATE) || material.equals(Material.IRON_CHESTPLATE) ||
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
                material.equals(Material.GOLD_LEGGINGS) || material.equals(Material.IRON_LEGGINGS) ||
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
                material.equals(Material.GOLD_BOOTS) || material.equals(Material.IRON_BOOTS) ||
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

    private static final int ARROW_DAMAGE = enchantMaxValueGetter(RandomItemsSettingsConfig.ARROW_DAMAGE_BOOL, RandomItemsSettingsConfig.ARROW_DAMAGE_MAX_LEVEL);
    private static final int ARROW_FIRE = enchantMaxValueGetter(RandomItemsSettingsConfig.ARROW_FIRE_BOOL, RandomItemsSettingsConfig.ARROW_FIRE_MAX_LEVEL);
    private static final int ARROW_INFINITE = enchantMaxValueGetter(RandomItemsSettingsConfig.ARROW_INFINITE_BOOL);
    private static final int ARROW_KNOCKBACK = enchantMaxValueGetter(RandomItemsSettingsConfig.ARROW_KNOCKBACK_BOOL, RandomItemsSettingsConfig.ARROW_KNOCKBACK_MAX_LEVEL);
    private static final int BINDING_CURSE = enchantMaxValueGetter(RandomItemsSettingsConfig.BINDING_CURSE_BOOL);
    private static final int DAMAGE_ALL = enchantMaxValueGetter(RandomItemsSettingsConfig.DAMAGE_ALL_BOOL, RandomItemsSettingsConfig.DAMAGE_ALL_MAX_LEVEL);
    private static final int DAMAGE_ARTHROPODS = enchantMaxValueGetter(RandomItemsSettingsConfig.DAMAGE_ARTHROPODS_BOOL, RandomItemsSettingsConfig.DAMAGE_ARTHROPODS_MAX_LEVEL);
    private static final int DAMAGE_UNDEAD = enchantMaxValueGetter(RandomItemsSettingsConfig.DAMAGE_UNDEAD_BOOL, RandomItemsSettingsConfig.DAMAGE_UNDEAD_MAX_LEVEL);
    private static final int DEPTH_STRIDER = enchantMaxValueGetter(RandomItemsSettingsConfig.DEPTH_STRIDER_BOOL, RandomItemsSettingsConfig.DEPTH_STRIDER_MAX_LEVEL);
    private static final int DIG_SPEED = enchantMaxValueGetter(RandomItemsSettingsConfig.DIG_SPEED_BOOL, RandomItemsSettingsConfig.DIG_SPEED_MAX_LEVEL);
    private static final int DURABILITY = enchantMaxValueGetter(RandomItemsSettingsConfig.DURABILITY_BOOL, RandomItemsSettingsConfig.DURABILITY_MAX_LEVEL);
    private static final int FIRE_ASPECT = enchantMaxValueGetter(RandomItemsSettingsConfig.FIRE_ASPECT_BOOL, RandomItemsSettingsConfig.FIRE_ASPECT_MAX_LEVEL);
    private static final int FROST_WALKER = enchantMaxValueGetter(RandomItemsSettingsConfig.FROST_WALKER_BOOL, RandomItemsSettingsConfig.FROST_WALKER_MAX_LEVEL);
    private static final int KNOCKBACK = enchantMaxValueGetter(RandomItemsSettingsConfig.KNOCKBACK_BOOL, RandomItemsSettingsConfig.KNOCKBACK_MAX_LEVEL);
    private static final int LOOT_BONUS_BLOCKS = enchantMaxValueGetter(RandomItemsSettingsConfig.LOOT_BONUS_BLOCKS_BOOL, RandomItemsSettingsConfig.LOOT_BONUS_BLOCKS_MAX_LEVEL);
    private static final int LOOT_BONUS_MOBS = enchantMaxValueGetter(RandomItemsSettingsConfig.LOOT_BONUS_MOBS_BOOL, RandomItemsSettingsConfig.LOOT_BONUS_MOBS_MAX_LEVEL);
    private static final int LUCK = enchantMaxValueGetter(RandomItemsSettingsConfig.LUCK_BOOL, RandomItemsSettingsConfig.LUCK_MAX_LEVEL);
    private static final int LURE = enchantMaxValueGetter(RandomItemsSettingsConfig.LURE_BOOL, RandomItemsSettingsConfig.LURE_MAX_LEVEL);
    private static final int MENDING = enchantMaxValueGetter(RandomItemsSettingsConfig.MENDING_BOOL);
    private static final int OXYGEN = enchantMaxValueGetter(RandomItemsSettingsConfig.OXYGEN_BOOL, RandomItemsSettingsConfig.OXYGEN_MAX_LEVEL);
    private static final int PROTECTION_ENVIRONMENTAL = enchantMaxValueGetter(RandomItemsSettingsConfig.PROTECTION_ENVIRONMENTAL_BOOL, RandomItemsSettingsConfig.PROTECTION_ENVIRONMENTAL_MAX_LEVEL);
    private static final int PROTECTION_EXPLOSIONS = enchantMaxValueGetter(RandomItemsSettingsConfig.PROTECTION_EXPLOSIONS_BOOL, RandomItemsSettingsConfig.PROTECTION_EXPLOSIONS_MAX_LEVEL);
    private static final int PROTECTION_FALL = enchantMaxValueGetter(RandomItemsSettingsConfig.PROTECTION_FALL_BOOL, RandomItemsSettingsConfig.PROTECTION_FALL_MAX_LEVEL);
    private static final int PROTECTION_FIRE = enchantMaxValueGetter(RandomItemsSettingsConfig.PROTECTION_FIRE_BOOL, RandomItemsSettingsConfig.PROTECTION_FIRE_MAX_LEVEL);
    private static final int PROTECTION_PROJECTILE = enchantMaxValueGetter(RandomItemsSettingsConfig.PROTECTION_PROJECTILE_BOOL, RandomItemsSettingsConfig.PROTECTION_PROJECTILE_MAX_LEVEL);
    private static final int SILK_TOUCH = enchantMaxValueGetter(RandomItemsSettingsConfig.SILK_TOUCH_BOOL);
    private static final int SWEEPING_EDGE = enchantMaxValueGetter(RandomItemsSettingsConfig.SWEEPING_EDGE_BOOL, RandomItemsSettingsConfig.SWEEPING_EDGE_MAX_LEVEL);
    private static final int THORNS = enchantMaxValueGetter(RandomItemsSettingsConfig.THORNS_BOOL, RandomItemsSettingsConfig.THORNS_MAX_LEVEL);
    private static final int VANISHING_CURSE = enchantMaxValueGetter(RandomItemsSettingsConfig.VANISHING_CURSE_BOOL);
    private static final int WATER_WORKER = enchantMaxValueGetter(RandomItemsSettingsConfig.WATER_WORKER_BOOL, RandomItemsSettingsConfig.WATER_WORKER_MAX_LEVEL);


    private static int enchantMaxValueGetter(String boolString) {

        Configuration configuration = ConfigValues.randomItemsConfig;

        if (configuration.getBoolean(boolString)) {

            return 1;

        }

        return 0;

    }

    private static int enchantMaxValueGetter(String boolString, String maxLevelString) {

        Configuration configuration = ConfigValues.randomItemsConfig;

        if (configuration.getBoolean(boolString)) {

            return configuration.getInt(maxLevelString);

        }

        return 0;

    }

}
