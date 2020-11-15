package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemTagger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

public class ItemEnchantmentPrevention implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        if (!ItemSettingsConfig.preventEliteItemEnchantment) {
            new EliteItemLore(event.getItem(), false);
            return;
        }
        if (ItemTagger.isEliteItem(event.getItem()))
            event.setCancelled(true);
    }
}
