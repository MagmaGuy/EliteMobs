package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class ItemEnchantmentPrevention implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onCombine(InventoryClickEvent event) {
        if (!event.getView().getTopInventory().getType().equals(InventoryType.ANVIL)) return;
        if (!ItemTagger.isEliteItem(event.getCurrentItem())) return;
        if (ItemSettingsConfig.preventEliteItemEnchantment) {
            event.setCancelled(true);
            return;
        }
        if (event.getSlot() != 2) return;
        new EliteItemLore(event.getCurrentItem(), false);
    }
}
