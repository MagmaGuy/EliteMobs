package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareSmithingEvent;

public class PreventUpgradeDiamondToNetherite implements Listener {
    @EventHandler
    public void UpgradeItemEvent(PrepareSmithingEvent event) {
        if (!ItemTagger.isEliteItem(event.getInventory().getItem(0)) &&
                !ItemTagger.isEliteItem(event.getInventory().getItem(1)))
            return;
        if (VersionChecker.serverVersionOlderThan(20, 0) ||
                event.getInventory().getItem(0) != null &&
                        event.getInventory().getItem(0).getType().equals(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE))
            event.setResult(ItemStackGenerator.generateItemStack(Material.AIR));
    }
}
