package com.magmaguy.elitemobs.events.actionevents;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.config.events.premade.KrakenEventConfig;
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
        if (!KrakenEventConfig.isEnabled) return;
        if (!EliteMobs.validWorldList.contains(event.getPlayer().getWorld())) return;
        if (!event.getPlayer().hasPermission("elitemobs.events.kraken")) return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getPlayer().getGameMode() == GameMode.SPECTATOR)
            return;
        if (event.getHook().getLocation().getBlock().isEmpty() || !event.getHook().getLocation().getBlock().isEmpty() &&
                !event.getHook().getLocation().getBlock().getType().equals(Material.WATER)) return;
        if (ThreadLocalRandom.current().nextDouble() > KrakenEventConfig.chanceOnFish)
            return;

        Kraken.spawnKraken(event.getHook().getLocation());

    }

}
