package com.magmaguy.elitemobs.events.actionevents;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class KrakenEvent implements Listener {

    @EventHandler
    public void onFishingStart(PlayerFishEvent event) {

        //todo: this is disabled for now, too many issues
        return;
        /**
         if (event.isCancelled()) return;
         if (!KrakenEventConfig.isEnabled) return;
         if (EliteMobs.worldguardIsEnabled &&
         !WorldGuardFlagChecker.checkFlag(event.getPlayer().getLocation(), (StateFlag) WorldGuardCompatibility.getElitemobsEventsFlag()))
         return;
         if (!EliteMobs.validWorldList.contains(event.getPlayer().getWorld())) return;
         if (!event.getPlayer().hasPermission("elitemobs.events.kraken")) return;
         if (event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getPlayer().getGameMode() == GameMode.SPECTATOR)
         return;
         if (event.getHook().getLocation().getBlock().isEmpty() || !event.getHook().getLocation().getBlock().isEmpty() &&
         !event.getHook().getLocation().getBlock().getType().equals(Material.WATER)) return;
         if (ThreadLocalRandom.current().nextDouble() > KrakenEventConfig.chanceOnFish)
         return;

         Kraken.spawnKraken(event.getHook().getLocation());
         */
    }

}
