package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
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

    @EventHandler(priority = EventPriority.LOWEST)
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

}
