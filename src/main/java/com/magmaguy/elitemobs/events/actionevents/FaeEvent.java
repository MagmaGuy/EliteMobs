package com.magmaguy.elitemobs.events.actionevents;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.events.mobs.Fae;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.concurrent.ThreadLocalRandom;

public class FaeEvent implements Listener {

    @EventHandler
    public void onTreeFell(BlockBreakEvent event) {

        if (event.isCancelled()) return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getPlayer().getGameMode() == GameMode.SPECTATOR) return;
        if (!(event.getBlock().getType().equals(Material.LOG) || event.getBlock().getType().equals(Material.LOG_2)))
            return;
//        if (!event.getPlayer().hasPermission("elitemobs.events.fae")) return;
        if (ThreadLocalRandom.current().nextDouble() > ConfigValues.eventsConfig.getDouble(EventsConfig.FAE_CHANCE_ON_CHOP))
            return;

        Fae.spawnFae(event.getBlock().getLocation());

    }

}
