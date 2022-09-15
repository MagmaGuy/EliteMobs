package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class ItemDisenchantPrevention implements Listener {
    @EventHandler
    public void onDisenchant(InventoryClickEvent event) {
        if (!event.getSlotType().equals(InventoryType.SlotType.RESULT)) return;
        if (!event.getInventory().getType().equals(InventoryType.GRINDSTONE)) return;
        if (!EliteItemManager.isEliteMobsItem(event.getInventory().getItem(0)) &&
                !EliteItemManager.isEliteMobsItem(event.getInventory().getItem(1))) return;
        event.setResult(Event.Result.DENY);
        event.setCancelled(true);
        event.getWhoClicked().sendMessage(ItemSettingsConfig.getPreventEliteItemDisenchantmentMessage());
        event.getWhoClicked().closeInventory();
    }
}
