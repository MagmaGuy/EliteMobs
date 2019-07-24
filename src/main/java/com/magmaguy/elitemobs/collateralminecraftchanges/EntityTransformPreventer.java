package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.EntityTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTransformEvent;

public class EntityTransformPreventer implements Listener {

    @EventHandler
    public void onMobTransform(EntityTransformEvent event) {
        if (EntityTracker.isEliteMob(event.getEntity())) event.setCancelled(true);
    }

}
