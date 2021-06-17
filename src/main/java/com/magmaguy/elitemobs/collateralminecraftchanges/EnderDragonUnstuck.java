package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class EnderDragonUnstuck implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDragonEnterCombat(EliteMobEnterCombatEvent event) {
        if (event.getEliteMobEntity() == null) return;
        if (event.getEliteMobEntity().getLivingEntity() == null) return;
        if (!event.getEliteMobEntity().getLivingEntity().getType().equals(EntityType.ENDER_DRAGON)) return;
        event.getEliteMobEntity().getLivingEntity().setAI(true);
        ((EnderDragon) event.getEliteMobEntity().getLivingEntity()).setPhase(EnderDragon.Phase.LEAVE_PORTAL);
    }
}
