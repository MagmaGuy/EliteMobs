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

package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.ItemsCustomLootSettingsConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlaceEventPrevent implements Listener {

    @EventHandler
    public void onPlaceForbiddenItem(PlayerInteractEvent event) {

        if (!ConfigValues.itemsCustomLootSettingsConfig.getBoolean(ItemsCustomLootSettingsConfig.PREVENT_CUSTOM_ITEM_PLACING))
            return;

        if (!event.isBlockInHand()) return;

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        if (event.getItem() == null || event.getItem().getItemMeta() == null) return;


        for (ItemStack itemStack : CustomItemConstructor.customItemList) {

            if (itemStack.getItemMeta().equals(event.getItem().getItemMeta()) && itemStack.getType().equals(event.getItem().getType())) {

                event.setCancelled(true);
                return;

            }

        }

    }

}
