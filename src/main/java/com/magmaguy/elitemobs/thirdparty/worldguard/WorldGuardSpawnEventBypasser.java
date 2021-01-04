package com.magmaguy.elitemobs.thirdparty.worldguard;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class WorldGuardSpawnEventBypasser implements Listener {

    private static boolean force = false;

    public static void forceSpawn() {
        force = true;
    }

    public static boolean isForcedSpawn() {
        return force;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!force) return;
        event.setCancelled(false);
        force = false;
    }

}
