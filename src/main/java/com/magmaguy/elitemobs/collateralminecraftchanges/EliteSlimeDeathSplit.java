package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SlimeSplitEvent;

public class EliteSlimeDeathSplit implements Listener {
    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void SlimeSplitEvent(SlimeSplitEvent event){
        if (EntityTracker.isEliteMob(event.getEntity())) event.setCancelled(true);
    }
}
