package com.magmaguy.elitemobs.playerdata;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerStatsTracker implements Listener {

    public static void registerPlayerDeath(Player player) {
        PlayerData.incrementDeaths(player.getUniqueId());
        PlayerData.decrementScore(player.getUniqueId());
    }

    @EventHandler
    public void onEliteDeath(EliteMobDeathEvent event) {
        if (event.getEliteMobEntity().getTriggeredAntiExploit()) return;
        if (event.getEliteMobEntity().getDamagers().isEmpty()) return;
        //todo : optimize
        for (Player player : event.getEliteMobEntity().getDamagers().keySet()) {
            PlayerData.incrementKills(player.getUniqueId());
            PlayerData.setHighestLevelKilled(player.getUniqueId(), event.getEliteMobEntity().getLevel());
            PlayerData.incrementScore(player.getUniqueId(), event.getEliteMobEntity().getLevel());
        }
    }
}
