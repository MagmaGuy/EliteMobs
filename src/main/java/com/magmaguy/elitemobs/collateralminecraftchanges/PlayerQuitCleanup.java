package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.utils.InvulnerabilityTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitCleanup implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (InvulnerabilityTracker.contains(event.getPlayer().getUniqueId()))
            event.getPlayer().setInvulnerable(false);
    }
}
