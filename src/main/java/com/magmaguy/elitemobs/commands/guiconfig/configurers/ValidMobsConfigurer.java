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

package com.magmaguy.elitemobs.commands.guiconfig.configurers;

import com.magmaguy.elitemobs.commands.guiconfig.GUIConfigHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ValidMobsConfigurer implements Listener {

    public static void validMobsPickerPopulator(Inventory inventory) {

        List<String> backToMainLore = new ArrayList<>(Arrays.asList("Back To Main Menu"));
        ItemStack backToMain = GUIConfigHandler.itemInitializer(Material.BARRIER, "Back To Main Menu", backToMainLore);
        inventory.setItem(0, backToMain);

    }

    private void repopulateValidMobs(Inventory inventory) {


    }

    @EventHandler
    public void validMobsConfigGUIInteraction(InventoryClickEvent event) {

        if (event.getClickedInventory() == null) {
            return;
        }

        if (event.getClickedInventory().getName().equals("Config GUI - Valid Mobs")) {

            event.setCancelled(true);

            if (event.getCurrentItem() != null && !event.getCurrentItem().getType().equals(Material.AIR)) {

                Player player = (Player) event.getWhoClicked();
                Inventory inventory = event.getInventory();
                ItemStack clickedItem = event.getCurrentItem();
                String name = clickedItem.getItemMeta().getDisplayName();

                if (name.equals("Back To Main Menu")) {

                    player.closeInventory();
                    Inventory smallInventory = GUIConfigHandler.threeRowInventory("Config GUI");
                    player.openInventory(smallInventory);
                    GUIConfigHandler.configPickerPopulator(smallInventory);

                    return;

                }

                if (name.equals("Configure Valid Elite Mobs")) {


                    return;

                }

            }

        }

    }

}
