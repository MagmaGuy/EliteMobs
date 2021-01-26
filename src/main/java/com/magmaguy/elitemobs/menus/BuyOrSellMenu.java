package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.menus.premade.BuyOrSellMenuConfig;
import com.magmaguy.elitemobs.utils.ObfuscatedStringHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BuyOrSellMenu implements Listener {

    private static final String SHOP_KEY = ObfuscatedStringHandler.obfuscateString("/");
    public static final String SHOP_NAME = BuyOrSellMenuConfig.SHOP_NAME + SHOP_KEY;

    public static void constructBuyOrSellMenu(Player player, ItemStack buyItemStack) {

        Inventory shopInventory = Bukkit.createInventory(player, 18, SHOP_NAME);

        //information item
        shopInventory.setItem(BuyOrSellMenuConfig.INFORMATION_SLOT, BuyOrSellMenuConfig.INFORMATION_ITEM);

        //sell item
        shopInventory.setItem(BuyOrSellMenuConfig.SELL_SLOT, BuyOrSellMenuConfig.SELL_ITEM);

        //buy item
        shopInventory.setItem(BuyOrSellMenuConfig.BUY_SLOT, buyItemStack);

        player.openInventory(shopInventory);

    }

    @EventHandler
    public void onInventoryInteraction(InventoryClickEvent event) {

        if (!SharedShopElements.sellMenuNullPointPreventer(event)) return;
        if (!event.getView().getTitle().equals(SHOP_NAME)) return;
        event.setCancelled(true);

        //info button
        if (event.getCurrentItem().equals(BuyOrSellMenuConfig.INFORMATION_ITEM)) {
            return;
        }

        //buy custom items button
        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(BuyOrSellMenuConfig.BUY_CUSTOM_ITEM.getItemMeta().getDisplayName())) {
            CustomShopMenu.customShopConstructor((Player) event.getWhoClicked());
            return;
        }

        //buy procedural items button
        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(BuyOrSellMenuConfig.BUY_PROCEDURAL_ITEM.getItemMeta().getDisplayName())) {
            ProceduralShopMenu.shopConstructor((Player) event.getWhoClicked());
            return;
        }

        //sell items button
        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(BuyOrSellMenuConfig.SELL_ITEM.getItemMeta().getDisplayName())) {
            SellMenu sellMenu = new SellMenu();
            sellMenu.constructSellMenu((Player) event.getWhoClicked());
            return;
        }

    }

}
