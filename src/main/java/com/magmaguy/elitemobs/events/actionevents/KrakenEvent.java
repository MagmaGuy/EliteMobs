package com.magmaguy.elitemobs.events.actionevents;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.events.mobs.Kraken;
import org.bukkit.Bukkit;
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
        if (!currentVersionUnder(13, 0)) return;
        if (!EliteMobs.validWorldList.contains(event.getPlayer().getWorld())) return;
//        if (!event.getPlayer().hasPermission("elitemobs.events.kraken")) return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getPlayer().getGameMode() == GameMode.SPECTATOR) return;
        if (event.getHook().getLocation().getBlock().isEmpty() || !event.getHook().getLocation().getBlock().isEmpty() &&
                !event.getHook().getLocation().getBlock().getType().equals(Material.WATER)) return;
        if (ThreadLocalRandom.current().nextDouble() > ConfigValues.eventsConfig.getDouble(EventsConfig.KRAKEN_CHANCE_ON_FISH))
            return;

        Kraken.spawnKraken(event.getHook().getLocation());

    }

    /*
    Don't run on 1.13 servers for now, will require a specific fix
     */
    private static boolean currentVersionUnder(int version, int subVersion) {

        if (Bukkit.getBukkitVersion().split("[.]").length < 4)
            return Integer.parseInt(Bukkit.getBukkitVersion().split("[.]")[1].substring(0, 2)) < version;

        return Integer.parseInt(Bukkit.getBukkitVersion().split("[.]")[1]) < version ||
                Integer.parseInt(Bukkit.getBukkitVersion().split("[.]")[1]) == version &&
                        Integer.parseInt(Bukkit.getBukkitVersion().split("[.]")[2].replace(")", "")) < subVersion;

    }

}
