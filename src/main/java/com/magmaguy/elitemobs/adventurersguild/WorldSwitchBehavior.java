package com.magmaguy.elitemobs.adventurersguild;

import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import org.bukkit.attribute.Attribute;
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
            event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
            //to a world with bonuses
        else if (!AdventurersGuildConfig.getWorldsWithoutAGBonuses().contains(event.getPlayer().getWorld().getName()) &&
                AdventurersGuildConfig.getWorldsWithoutAGBonuses().contains(event.getFrom().getName()))
            GuildRank.setMaxHealth(event.getPlayer());
    }
}
