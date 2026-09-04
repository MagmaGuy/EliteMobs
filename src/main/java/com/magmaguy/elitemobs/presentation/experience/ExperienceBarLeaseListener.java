package com.magmaguy.elitemobs.presentation.experience;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/** Reasserts client-only XP ownership after Bukkit sends authoritative experience packets. */
public final class ExperienceBarLeaseListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onExperienceChanged(PlayerExpChangeEvent event) {
        rerenderNextTick(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLevelChanged(PlayerLevelChangeEvent event) {
        rerenderNextTick(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChanged(PlayerChangedWorldEvent event) {
        rerenderNextTick(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        rerenderNextTick(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        ExperienceBarLease.discard(event.getPlayer());
    }

    private static void rerenderNextTick(org.bukkit.entity.Player player) {
        if (!ExperienceBarLease.isLeased(player)) return;
        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            if (player.isOnline()) ExperienceBarLease.rerender(player);
        });
    }
}
