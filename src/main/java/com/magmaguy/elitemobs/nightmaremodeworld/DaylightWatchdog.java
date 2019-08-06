package com.magmaguy.elitemobs.nightmaremodeworld;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class DaylightWatchdog implements Listener {

    public static void preventDaylight(World world) {

        new BukkitRunnable() {
            @Override
            public void run() {
                long time = world.getTime();
                if (time < 12000)
                    world.setTime(time + 4);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    @EventHandler
    public void onSleep(PlayerBedEnterEvent event) {
        if (EliteMobs.nightmareWorlds.contains(event.getPlayer().getWorld())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColorConverter.convert("&7[EM] &4Sleep can't save you now..."));
        }
    }

}
