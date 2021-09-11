package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.config.ValidWorldsConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class FindNewWorlds implements Listener {

    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onWorldLoad(WorldLoadEvent event) {
        ValidWorldsConfig.addWorld(event.getWorld().getName());
    }

}
