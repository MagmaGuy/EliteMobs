package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlaceEventPrevent implements Listener {

    @EventHandler
    public void onPlaceForbiddenItem(PlayerInteractEvent event) {

        if (!ItemSettingsConfig.isPreventCustomItemPlacement())
            return;

        if (!event.isBlockInHand()) return;

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        if (event.getItem() == null || event.getItem().getItemMeta() == null) return;

        if (ItemTagger.isEliteItem(event.getItem()))
            event.setCancelled(true);

    }

}
