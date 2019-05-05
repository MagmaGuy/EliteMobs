package com.magmaguy.elitemobs.events.actionevents;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.events.mobs.Kraken;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.concurrent.ThreadLocalRandom;

public class KrakenEvent implements Listener {

    @EventHandler
    public void onFishingStart(PlayerFishEvent event) {

        if (event.isCancelled()) return;
        if (!ConfigValues.eventsConfig.getBoolean(EventsConfig.KRAKEN_ENABLED)) return;
        if (!EliteMobs.validWorldList.contains(event.getPlayer().getWorld())) return;
        if (!event.getPlayer().hasPermission("elitemobs.events.kraken")) return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getPlayer().getGameMode() == GameMode.SPECTATOR)
            return;
        if (event.getHook().getLocation().getBlock().isEmpty() || !event.getHook().getLocation().getBlock().isEmpty() &&
                !event.getHook().getLocation().getBlock().getType().equals(Material.WATER)) return;
        if (ThreadLocalRandom.current().nextDouble() > ConfigValues.eventsConfig.getDouble(EventsConfig.KRAKEN_CHANCE_ON_FISH))
            return;

        Kraken.spawnKraken(event.getHook().getLocation());

    }

}
