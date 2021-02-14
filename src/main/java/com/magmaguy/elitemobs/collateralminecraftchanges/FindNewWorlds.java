package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.npcs.NPCInitializer;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.ValidWorldsConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class FindNewWorlds implements Listener {

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        ValidWorldsConfig.initializeConfig();
        if (event.getWorld().getName().equals(AdventurersGuildConfig.guildWorldName))
            new NPCInitializer(event.getWorld());
    }

}
