package com.magmaguy.elitemobs.initialsetup;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class FirstTimeSetup implements Listener {
    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {
        if (DefaultConfig.isSetupDone()) return;
        if (!event.getPlayer().hasPermission("elitemobs.*")) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!event.getPlayer().isOnline()) return;
                Logger.sendSimpleMessage(event.getPlayer(), "&8&m----------------------------------------------------");
                Logger.sendSimpleMessage(event.getPlayer(), "&7[EliteMobs] &fInitial setup message:");
                Logger.sendSimpleMessage(event.getPlayer(), "&7Welcome to EliteMobs!" +
                        " &c&lIt looks like have not have set up EliteMobs yet! &2To install EliteMobs, do &a/em initialize &2!");
                Logger.sendSimpleMessage(event.getPlayer(), "&7Need command help? &6&l/em help");
                Logger.sendSimpleMessage(event.getPlayer(), "&7You can get support over at &9&n" + DiscordLinks.mainLink);
                Logger.sendSimpleMessage(event.getPlayer(), "&cPick an option in /em setup to permanently dismiss this message!");
                Logger.sendSimpleMessage(event.getPlayer(), "&8&m----------------------------------------------------");
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 10);
    }
}
