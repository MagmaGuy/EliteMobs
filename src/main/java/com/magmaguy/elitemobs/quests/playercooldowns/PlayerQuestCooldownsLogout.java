package com.magmaguy.elitemobs.quests.playercooldowns;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuestCooldownsLogout implements Listener {
    @EventHandler
    public static void onPlayerLogout(PlayerQuitEvent event) {
        PlayerQuestCooldowns.flushPlayer(event.getPlayer());
    }
}
