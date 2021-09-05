package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.api.EliteMobsItemDetector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class AlternativeDurabilityLoss implements Listener {
    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDurabilityLoss(PlayerItemDamageEvent event) {
        if (!EliteMobsItemDetector.isEliteMobsItem(event.getItem())) return;
        event.setCancelled(true);
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){

    }
}
