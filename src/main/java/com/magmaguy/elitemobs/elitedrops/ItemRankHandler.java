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

import org.bukkit.Material;

/**
 * Created by MagmaGuy on 06/06/2017.
 */
public class ItemRankHandler {

    public static int guessItemRank (Material material, int enchantmentTotal) {

        int itemTypePowerCount = itemTypePower(material);

        int total = itemTypePowerCount + enchantmentTotal;

        return total;

    }

    public static int itemTypePower (Material material) {

        switch (material) {

            case DIAMOND:
                return 5;
            case DIAMOND_AXE:
                return 5;
            case DIAMOND_BARDING:
                return 5;
            case DIAMOND_BLOCK:
                return 5;
            case DIAMOND_BOOTS:
                return 5;
            case DIAMOND_CHESTPLATE:
                return 5;
            case DIAMOND_HELMET:
                return 5;
            case DIAMOND_HOE:
                return 5;
            case DIAMOND_LEGGINGS:
                return 5;
            case DIAMOND_ORE:
                return 5;
            case DIAMOND_PICKAXE:
                return 5;
            case DIAMOND_SPADE:
                return 5;
            case DIAMOND_SWORD:
                return 5;
            case IRON_AXE:
                return 4;
            case IRON_BARDING:
                return 4;
            case IRON_BLOCK:
                return 4;
            case IRON_BOOTS:
                return 4;
            case IRON_CHESTPLATE:
                return 4;
            case IRON_HELMET:
                return 4;
            case IRON_HOE:
                return 4;
            case IRON_INGOT:
                return 4;
            case IRON_LEGGINGS:
                return 4;
            case IRON_NUGGET:
                return 4;
            case IRON_ORE:
                return 4;
            case IRON_PICKAXE:
                return 4;
            case IRON_SPADE:
                return 4;
            case IRON_SWORD:
                return 4;
            case CHAINMAIL_BOOTS:
                return 3;
            case CHAINMAIL_CHESTPLATE:
                return 3;
            case CHAINMAIL_HELMET:
                return 3;
            case CHAINMAIL_LEGGINGS:
                return 3;
            case GOLD_AXE:
                return 2;
            case GOLD_BARDING:
                return 2;
            case GOLD_BLOCK:
                return 2;
            case GOLD_BOOTS:
                return 2;
            case GOLD_CHESTPLATE:
                return 2;
            case GOLD_HELMET:
                return 2;
            case GOLD_HOE:
                return 2;
            case GOLD_INGOT:
                return 2;
            case GOLD_LEGGINGS:
                return 2;
            case GOLD_NUGGET:
                return 2;
            case GOLD_ORE:
                return 2;
            case GOLD_PICKAXE:
                return 2;
            case GOLD_SPADE:
                return 2;
            case GOLD_SWORD:
                return 2;
            case GOLDEN_APPLE:
                return 2;
            case GOLDEN_CARROT:
                return 2;
            case LEATHER_BOOTS:
                return 1;
            case LEATHER_CHESTPLATE:
                return 1;
            case LEATHER_HELMET:
                return 1;
            case LEATHER_LEGGINGS:
                return 1;
            default:
                return 0;

        }

    }

}
