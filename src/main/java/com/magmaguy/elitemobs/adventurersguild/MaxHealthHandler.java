package com.magmaguy.elitemobs.adventurersguild;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class MaxHealthHandler implements Listener {

    public static void adjustMaxHealth(Player player) {

        int guildRank = GuildRank.getRank(player);
        guildRank--;
        if (guildRank < 0)
            guildRank = 0;

        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() != guildRank * 2 + 20)
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(guildRank * 2 + 20);

    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                adjustMaxHealth(event.getPlayer());
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20);
    }

}
