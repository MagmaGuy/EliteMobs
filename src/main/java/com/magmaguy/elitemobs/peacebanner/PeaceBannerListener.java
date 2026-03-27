package com.magmaguy.elitemobs.peacebanner;

import com.magmaguy.elitemobs.config.PeaceBannerConfig;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class PeaceBannerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!PeaceBannerConfig.isEnabled()) return;
        if (!PeaceBannerItem.isPeaceBanner(event.getItemInHand())) return;
        PeaceBannerManager.registerBanner(event.getBlock().getLocation());
        event.getPlayer().sendMessage(ChatColorConverter.convert(
                PeaceBannerConfig.getPlacedMessage()
                        .replace("$radius", String.valueOf(PeaceBannerConfig.getChunkRadius()))));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!PeaceBannerConfig.isEnabled()) return;
        Block block = event.getBlock();
        if (!block.getType().name().contains("BANNER")) return;
        if (PeaceBannerManager.unregisterBanner(block.getLocation())) {
            event.getPlayer().sendMessage(ChatColorConverter.convert(PeaceBannerConfig.getRemovedMessage()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!PeaceBannerConfig.isEnabled()) return;
        PeaceBannerManager.validateChunk(event.getChunk());
    }
}
