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

package com.magmaguy.elitemobs.commands.guiconfig;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MagmaGuy on 21/10/2017.
 */
public class GUIConfigHandler implements Listener {

    public void GUIConfigHandler(Player player) {

        Inventory tier1SelectorInventory = Bukkit.createInventory(null, 9 * 4, "EliteMobs config picker");

        ItemStack signature = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta signatureSkullMeta = (SkullMeta) signature.getItemMeta();
        signatureSkullMeta.setOwner("magmaguy");
        signatureSkullMeta.setDisplayName("EliteMobs by MagmaGuy");
        List<String> signatureList = new ArrayList<>();
        signatureList.add("Support the plugins you enjoy!");
        signatureSkullMeta.setLore(signatureList);
        signature.setItemMeta(signatureSkullMeta);

        tier1SelectorInventory.setItem(4, signature);

        ItemStack mobItemStack = new ItemStack(Material.PAPER, 1);
        ItemMeta mobItemMeta = mobItemStack.getItemMeta();
        mobItemMeta.setDisplayName("Mobs");
        List<String> mobItemStackLore = new ArrayList<>();
        mobItemStackLore.add("Click me to configure mob-related things");
        mobItemMeta.setLore(mobItemStackLore);
        mobItemStack.setItemMeta(mobItemMeta);

        tier1SelectorInventory.setItem(20, mobItemStack);

        ItemStack itemItemStack = new ItemStack(Material.IRON_SWORD, 1);
        ItemMeta itemItemMeta = itemItemStack.getItemMeta();

    }

}
