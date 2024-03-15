package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerDeathEvent implements Listener {
    @EventHandler
    public void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.hasPermission("elitemobs.death.bypass")) return;
        int guildMaxRank = GuildRank.getMaxGuildRank(player);
        if (guildMaxRank - 1 <= 0) return;
        GuildRank.setMaxGuildRank(player, guildMaxRank - 1);
    }
}
