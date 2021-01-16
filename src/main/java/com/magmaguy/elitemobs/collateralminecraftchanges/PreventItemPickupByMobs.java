package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class PreventItemPickupByMobs implements Listener {

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (EntityTracker.isEliteMob(event.getEntity()))
            event.setCancelled(true);
    }

}
