package com.magmaguy.elitemobs.adventurersguild;

import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.magmacore.util.AttributeManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WorldSwitchBehavior implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerChangedWorldEvent event) {
        if (AdventurersGuildConfig.getWorldsWithoutAGBonuses().isEmpty()) return;
        //to a world without bonuses
        if (AdventurersGuildConfig.getWorldsWithoutAGBonuses().contains(event.getPlayer().getWorld().getName()) &&
                !AdventurersGuildConfig.getWorldsWithoutAGBonuses().contains(event.getFrom().getName()))
            AttributeManager.setAttribute(event.getPlayer(), "generic_max_health", AttributeManager.getAttributeDefaultValue(event.getPlayer(), "generic_max_health"));
            //to a world with bonuses
        else if (!AdventurersGuildConfig.getWorldsWithoutAGBonuses().contains(event.getPlayer().getWorld().getName()) &&
                AdventurersGuildConfig.getWorldsWithoutAGBonuses().contains(event.getFrom().getName()))
            GuildRank.setMaxHealth(event.getPlayer());
    }
}
