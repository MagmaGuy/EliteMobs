package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTransformEvent;

public class EliteSlimeDeathSplit implements Listener {
    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSlimeSplit(EntityTransformEvent event) {
        if (event.getTransformReason() != EntityTransformEvent.TransformReason.SPLIT
                || (!(event.getEntity() instanceof Slime) && !(event.getEntity() instanceof MagmaCube))) return;
        // The actor may already be unregistered on death; the persistent tag still identifies it.
        if (EntityTracker.isEliteMob(event.getEntity())) event.setCancelled(true);
    }
}
