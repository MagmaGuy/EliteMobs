package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTransformEvent;

public class EntityTransformHandler implements Listener {

    @EventHandler
    public void onMobTransform(EntityTransformEvent event) {
        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());
        if (eliteEntity == null) return;
        event.setCancelled(true);
    }

}
