package com.magmaguy.elitemobs.quests.playercooldowns;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuestCooldownsLogout implements Listener {
    @EventHandler
    public static void onPlayerLogout(PlayerQuitEvent event) {
        com.magmaguy.elitemobs.quests.Quest.releasePlayerSession(event.getPlayer().getUniqueId());
        PlayerQuestCooldowns.flushPlayer(event.getPlayer());
    }
}
