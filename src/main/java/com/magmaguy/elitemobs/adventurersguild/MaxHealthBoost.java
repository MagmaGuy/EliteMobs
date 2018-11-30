package com.magmaguy.elitemobs.adventurersguild;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.playerdata.PlayerData;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class MaxHealthBoost implements Listener {

    @EventHandler
    public void playerLogin(PlayerLoginEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!PlayerData.playerMaxGuildRank.containsKey(event.getPlayer().getUniqueId())) return;
                if (PlayerData.playerMaxGuildRank.get(event.getPlayer().getUniqueId()) < 11) return;

                event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue((PlayerData.playerMaxGuildRank.get(event.getPlayer().getUniqueId()) - 10) * 2 + 20);

            }
        }.runTaskLater(MetadataHandler.PLUGIN, 5);
    }

}
