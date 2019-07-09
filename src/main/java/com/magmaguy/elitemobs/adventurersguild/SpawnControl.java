package com.magmaguy.elitemobs.adventurersguild;

import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class SpawnControl implements Listener {

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {

        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM)) return;
        if (!event.getLocation().getWorld().getName().equals(AdventurersGuildConfig.guildWorldName))
            return;

        event.setCancelled(true);

    }

}
