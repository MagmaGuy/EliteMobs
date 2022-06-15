package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTransformEvent;

public class NPCsBecomeWitches implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNPCConversion(EntityTransformEvent event) {
        if (!EntityTracker.isNPCEntity(event.getEntity())) return;
        event.setCancelled(true);
    }
}
