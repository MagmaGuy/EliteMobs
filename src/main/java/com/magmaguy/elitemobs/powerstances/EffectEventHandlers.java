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

package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.EntityTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 * Created by MagmaGuy on 14/07/2017.
 */
public class EffectEventHandlers implements Listener {

    @EventHandler
    public void playerPickupSafeguard(PlayerPickupItemEvent event) {
        if (EntityTracker.isItemVisualEffect(event.getItem()))
            event.setCancelled(true);
    }

    @EventHandler
    public void hopperPickupSafeguard(InventoryPickupItemEvent event) {
        if (EntityTracker.isItemVisualEffect(event.getItem()))
            event.setCancelled(true);
    }

    //todo: this prevents elite mobs from going through portals, might want to change it at some point
    @EventHandler
    public void portalPickupSafeguard(EntityPortalEvent event) {
        if (EntityTracker.isItemVisualEffect(event.getEntity()) || EntityTracker.isEliteMob(event.getEntity()))
            event.setCancelled(true);
    }

    @EventHandler
    public void itemDespawnPrevention(ItemDespawnEvent event) {
        if (EntityTracker.isItemVisualEffect(event.getEntity()))
            event.setCancelled(true);
    }

}
