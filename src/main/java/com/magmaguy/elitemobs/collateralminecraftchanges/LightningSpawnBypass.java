package com.magmaguy.elitemobs.collateralminecraftchanges;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class LightningSpawnBypass implements Listener {
    private static boolean bypass = false;

    public static void strikeLightingIgnoreProtections(Location location) {
        if (location == null || location.getWorld() == null) return;
        bypass = true;
        location.getWorld().strikeLightningEffect(location);
    }

    public static void bypass() {
        bypass = true;
    }

    //This overrides region protection plugins that would delete lightning - breaking powers and effects
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLightningSpawn(EntitySpawnEvent event) {
        if (!bypass) return;
        if (!event.getEntity().getType().equals(EntityType.LIGHTNING)) return;
        bypass = true;
        event.setCancelled(false);
    }
}
