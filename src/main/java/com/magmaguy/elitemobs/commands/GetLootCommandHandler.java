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

package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.items.uniqueitems.UniqueItemInitializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static com.magmaguy.elitemobs.items.CustomItemConstructor.customItemList;

/**
 * Created by MagmaGuy on 01/05/2017.
 */
public class GetLootCommandHandler {

    public static boolean getLoot(Player player, String args1) {

        boolean itemExists = false;

        for (ItemStack itemStack : customItemList)
            itemExists = getItem(itemStack, args1, player);

        if (itemExists) return true;

        for (ItemStack itemStack : UniqueItemInitializer.uniqueItemsList)
            itemExists = getItem(itemStack, args1, player);

        if (itemExists) return true;
        return false;

    }

    private static boolean getItem(ItemStack itemStack, String args1, Player player) {

        String itemRawName = itemStack.getItemMeta().getDisplayName();

        if (itemRawName != null) {

            Bukkit.getLogger().info(itemStack.getItemMeta().getDisplayName());

            String itemProcessedName = itemRawName.replaceAll(" ", "_").toLowerCase();
            itemProcessedName = ChatColor.stripColor(itemProcessedName);

            if (itemProcessedName.equalsIgnoreCase(args1) && player.isValid()) {

                player.getInventory().addItem(itemStack);

                return true;

            }

        }

        return false;

    }

}
