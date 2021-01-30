package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class PreventEliteSilverfishBlockEnter implements Listener {
    @EventHandler
    public void onSilverfishBlockEnter(EntityChangeBlockEvent event) {
        if (EntityTracker.isEliteMob(event.getEntity()))
            event.setCancelled(true);
    }
}
