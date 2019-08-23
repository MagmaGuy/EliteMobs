package com.magmaguy.elitemobs.worldguard;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.EntityTracker;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class WorldGuardEliteMobOnlyFlag implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!EliteMobs.worldguardIsEnabled) return;
        if (EntityTracker.isEliteMob(event.getEntity())) return;
        if (!WorldGuardFlagChecker.checkFlag(event.getLocation(), (StateFlag) WorldGuardCompatibility.getEliteMobsOnlySpawnFlag())) {
            event.setCancelled(true);
            event.getEntity().remove();
        }
    }

}
