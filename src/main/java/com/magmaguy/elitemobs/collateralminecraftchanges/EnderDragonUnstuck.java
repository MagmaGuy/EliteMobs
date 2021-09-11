package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class EnderDragonUnstuck implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDragonEnterCombat(EliteMobEnterCombatEvent event) {
        if (event.getEliteMobEntity() == null) return;
        if (event.getEliteMobEntity().getLivingEntity() == null) return;
        if (!event.getEliteMobEntity().getLivingEntity().getType().equals(EntityType.ENDER_DRAGON)) return;
        event.getEliteMobEntity().getLivingEntity().setAI(true);
        ((EnderDragon) event.getEliteMobEntity().getLivingEntity()).setPhase(EnderDragon.Phase.LEAVE_PORTAL);
    }

    public static class AggroPrevention implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onTarget(EntityTargetLivingEntityEvent event) {
            if (event.getEntity().getType().equals(EntityType.WOLF) && !((Wolf) event.getEntity()).isAngry() ||
                    event.getTarget() != null &&
                            event.getTarget().getType().equals(EntityType.WOLF) && !((Wolf) event.getTarget()).isAngry())
                return;
            if (EntityTracker.isEliteMob(event.getEntity()) && event.getTarget() != null && EntityTracker.isEliteMob(event.getTarget()) ||
                    EntityTracker.isEliteMob(event.getEntity()) && event.getTarget() != null && EntityTracker.isNPCEntity(event.getTarget()))
                event.setCancelled(true);
        }

    }
}
