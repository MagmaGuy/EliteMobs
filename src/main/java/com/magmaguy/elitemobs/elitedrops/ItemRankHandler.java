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

import com.magmaguy.elitemobs.mobcustomizer.DamageAdjuster;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Created by MagmaGuy on 06/06/2017.
 */
public class ItemRankHandler {

    /*
    Item rank directly translates to Elite Mob level increases
     */

    public static int guessItemThreat(ItemStack itemStack) {

        double itemTypePowerCount = itemTypePower(itemStack.getType());

        int enchantmentCount = countEnchantments(itemStack);

        double total = itemTypePowerCount + enchantmentCount;

        return (int) total;

    }

    public static double itemTypePower(Material material) {

        //Divide the tier rank by the 4 armor slots and 1 weapon slot
        double diamondLevel = DamageAdjuster.DIAMOND_TIER_LEVEL / 5;
        double ironLevel = DamageAdjuster.IRON_TIER_LEVEL / 5;
        double stoneChainLevel = DamageAdjuster.STONE_CHAIN_TIER_LEVEL / 5;
        double goldWoodLeatherLevel = DamageAdjuster.GOLD_WOOD_LEATHER_TIER_LEVEL / 5;

        switch (material) {

            case DIAMOND:
                return diamondLevel;
            case DIAMOND_AXE:
                return diamondLevel;
            case DIAMOND_BARDING:
                return diamondLevel;
            case DIAMOND_BLOCK:
                return diamondLevel;
            case DIAMOND_BOOTS:
                return diamondLevel;
            case DIAMOND_CHESTPLATE:
                return diamondLevel;
            case DIAMOND_HELMET:
                return diamondLevel;
            case DIAMOND_HOE:
                return diamondLevel;
            case DIAMOND_LEGGINGS:
                return diamondLevel;
            case DIAMOND_ORE:
                return diamondLevel;
            case DIAMOND_PICKAXE:
                return diamondLevel;
            case DIAMOND_SPADE:
                return diamondLevel;
            case DIAMOND_SWORD:
                return diamondLevel;
            case IRON_AXE:
                return ironLevel;
            case IRON_BARDING:
                return ironLevel;
            case IRON_BLOCK:
                return ironLevel;
            case IRON_BOOTS:
                return ironLevel;
            case IRON_CHESTPLATE:
                return ironLevel;
            case IRON_HELMET:
                return ironLevel;
            case IRON_HOE:
                return ironLevel;
            case IRON_INGOT:
                return ironLevel;
            case IRON_LEGGINGS:
                return ironLevel;
            case IRON_NUGGET:
                return ironLevel;
            case IRON_ORE:
                return ironLevel;
            case IRON_PICKAXE:
                return ironLevel;
            case IRON_SPADE:
                return ironLevel;
            case IRON_SWORD:
                return ironLevel;
            case CHAINMAIL_BOOTS:
                return stoneChainLevel;
            case CHAINMAIL_CHESTPLATE:
                return stoneChainLevel;
            case CHAINMAIL_HELMET:
                return stoneChainLevel;
            case CHAINMAIL_LEGGINGS:
                return stoneChainLevel;
            case STONE_SWORD:
                return stoneChainLevel;
            case GOLD_AXE:
                return goldWoodLeatherLevel;
            case GOLD_BARDING:
                return goldWoodLeatherLevel;
            case GOLD_BLOCK:
                return goldWoodLeatherLevel;
            case GOLD_BOOTS:
                return goldWoodLeatherLevel;
            case GOLD_CHESTPLATE:
                return goldWoodLeatherLevel;
            case GOLD_HELMET:
                return goldWoodLeatherLevel;
            case GOLD_HOE:
                return goldWoodLeatherLevel;
            case GOLD_INGOT:
                return goldWoodLeatherLevel;
            case GOLD_LEGGINGS:
                return goldWoodLeatherLevel;
            case GOLD_NUGGET:
                return goldWoodLeatherLevel;
            case GOLD_ORE:
                return goldWoodLeatherLevel;
            case GOLD_PICKAXE:
                return goldWoodLeatherLevel;
            case GOLD_SPADE:
                return goldWoodLeatherLevel;
            case GOLD_SWORD:
                return goldWoodLeatherLevel;
            case GOLDEN_APPLE:
                return goldWoodLeatherLevel;
            case GOLDEN_CARROT:
                return goldWoodLeatherLevel;
            case LEATHER_BOOTS:
                return goldWoodLeatherLevel;
            case LEATHER_CHESTPLATE:
                return goldWoodLeatherLevel;
            case LEATHER_HELMET:
                return goldWoodLeatherLevel;
            case LEATHER_LEGGINGS:
                return goldWoodLeatherLevel;
            case WOOD_SWORD:
                return goldWoodLeatherLevel;
            case WOOD_AXE:
                return goldWoodLeatherLevel;
            case WOOD_PICKAXE:
                return goldWoodLeatherLevel;
            case WOOD_HOE:
                return goldWoodLeatherLevel;
            default:
                return 0;

        }

    }

    private static int countEnchantments(ItemStack itemStack) {

        int enchantments = 0;

        if (!itemStack.getEnchantments().isEmpty()) {

            int enchantmentCount = 0;

            for (Map.Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {

                if (entry.getKey().equals(Enchantment.PROTECTION_ENVIRONMENTAL)) {

                    enchantments += entry.getValue();

                }

                if (entry.getKey().equals(Enchantment.THORNS)) {

                    enchantmentCount += entry.getValue();

                }

                if (entry.getKey().equals(Enchantment.DAMAGE_ALL)) {

                    enchantmentCount += entry.getValue();

                }

                if (entry.getKey().equals(Enchantment.ARROW_DAMAGE)) {

                    enchantmentCount += entry.getValue();

                }

            }

            enchantments += enchantmentCount;

        }

        //multiply my 4/5 because a full set of enchants involves 4 protection and 1 offensive
        return (int) (enchantments * DamageAdjuster.ENCHANTMENT_OR_POTION_EFFECT_THREAT_INCREMENTER);

    }

}
