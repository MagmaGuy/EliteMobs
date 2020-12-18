package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.entity.IronGolem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class AggroPrevention implements Listener {

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent event) {

        if (EntityTracker.isEliteMob(event.getEntity()) &&
                event.getTarget() != null && EntityTracker.isEliteMob(event.getTarget()) &&
                !(event.getTarget() instanceof IronGolem) && !(event.getEntity() instanceof IronGolem))
            event.setCancelled(true);

    }

}
