package com.magmaguy.elitemobs.thirdparty.worldguard;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class WorldGuardEliteMobOnlySpawnFlag implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!EliteMobs.worldguardIsEnabled) return;
        if (event.getEntity().getType().equals(EntityType.ARMOR_STAND) ||
                event.getEntity().getType().equals(EntityType.VILLAGER) && event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM))
            return;
        if (WorldGuardFlagChecker.checkFlag(event.getLocation(), WorldGuardCompatibility.getEliteMobsOnlySpawnFlag())) {
            if (EntityTracker.isEliteMob(event.getEntity())) return;
            event.setCancelled(true);
            event.getEntity().remove();
        }
    }

}
