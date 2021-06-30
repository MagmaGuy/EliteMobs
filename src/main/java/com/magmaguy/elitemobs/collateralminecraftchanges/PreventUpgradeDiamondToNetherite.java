package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
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
        event.setResult(ItemStackGenerator.generateItemStack(Material.AIR));
    }
}
