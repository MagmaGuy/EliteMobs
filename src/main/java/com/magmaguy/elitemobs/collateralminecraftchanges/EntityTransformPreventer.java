package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTransformEvent;

public class EntityTransformPreventer implements Listener {

    @EventHandler
    public void onMobTransform(EntityTransformEvent event) {
        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
        if (eliteMobEntity == null) return;
        event.setCancelled(true);
        if (event.getTransformReason().equals(EntityTransformEvent.TransformReason.DROWNED)) {
            event.getEntity().remove();
//            SpiritWalk spiritWalk = new SpiritWalk();
//            spiritWalk.initializeSpiritWalk(eliteMobEntity.getLivingEntity());
        }

    }

}
