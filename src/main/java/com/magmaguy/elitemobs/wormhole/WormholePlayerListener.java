package com.magmaguy.elitemobs.wormhole;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/** Cooldowns own spatial departure checks; native displays own their viewer lifecycle. */
public class WormholePlayerListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        WormholeManager manager = WormholeManager.getInstance(true);
        if (manager != null) manager.getPlayerTeleportData().remove(event.getPlayer().getUniqueId());
    }
}
