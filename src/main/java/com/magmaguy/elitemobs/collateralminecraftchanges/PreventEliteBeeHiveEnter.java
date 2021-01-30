package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEnterBlockEvent;

public class PreventEliteBeeHiveEnter implements Listener {
    @EventHandler
    public void onBeeEnterHive(EntityEnterBlockEvent event) {
        if (EntityTracker.isEliteMob(event.getEntity()))
            event.setCancelled(true);
    }
}
