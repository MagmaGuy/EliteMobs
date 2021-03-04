package com.magmaguy.elitemobs.versionnotifier;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class VersionWarner implements Listener {

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {

        if (!event.getPlayer().hasPermission("elitemobs.versionnotification")) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                event.getPlayer().sendMessage(ChatColorConverter.convert("&a[EliteMobs] &cYour version of EliteMobs is outdated." +
                        " &aYou can download the latest version from &3&n&ohttps://www.spigotmc.org/resources/%E2%9A%94elitemobs%E2%9A%94.40090/"));
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 3);


    }

}
