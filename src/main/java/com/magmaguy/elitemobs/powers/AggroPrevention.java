package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class AggroPrevention implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (event.getEntity().getType().equals(EntityType.WOLF) && !((Wolf) event.getEntity()).isAngry() ||
                event.getTarget() != null &&
                        event.getTarget().getType().equals(EntityType.WOLF) &&
                        !((Wolf) event.getTarget()).isAngry())
            return;
        if (EntityTracker.isEliteMob(event.getEntity()) &&
                event.getTarget() != null && EntityTracker.isEliteMob(event.getTarget()) &&
                !(event.getTarget() instanceof IronGolem) && !(event.getEntity() instanceof IronGolem))
            event.setCancelled(true);
    }

}
