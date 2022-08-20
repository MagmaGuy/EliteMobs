package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ResourcePackDataConfig;
import com.magmaguy.elitemobs.config.menus.premade.BuyOrSellMenuConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

import static com.magmaguy.elitemobs.menus.BuyOrSellMenu.BuyOrSellMenuEvents.menus;

public class BuyOrSellMenu {

    public static void constructBuyOrSellMenu(Player player, ItemStack buyItemStack) {
        String inventoryName = BuyOrSellMenuConfig.SHOP_NAME;
        if (ResourcePackDataConfig.isDisplayCustomMenuUnicodes())
            inventoryName = ChatColor.WHITE + "\uF801\uDB80\uDC7B\uF805       " + inventoryName;

        Inventory shopInventory = Bukkit.createInventory(player, 18, inventoryName);
        menus.put(player, shopInventory);
        //information item
        ItemStack info = BuyOrSellMenuConfig.INFORMATION_ITEM;
        if (ResourcePackDataConfig.isDisplayCustomMenuUnicodes()) {
            info.setType(Material.PAPER);
            ItemMeta itemMeta = info.getItemMeta();
            itemMeta.setCustomModelData(MetadataHandler.signatureID);
            info.setItemMeta(itemMeta);
        }

        shopInventory.setItem(BuyOrSellMenuConfig.INFORMATION_SLOT, info);
        //sell item
        shopInventory.setItem(BuyOrSellMenuConfig.SELL_SLOT, BuyOrSellMenuConfig.SELL_ITEM);
        //buy item
        shopInventory.setItem(BuyOrSellMenuConfig.BUY_SLOT, buyItemStack);
        player.openInventory(shopInventory);
    }

    public static class BuyOrSellMenuEvents implements Listener {
        public static final Map<Player, Inventory> menus = new HashMap<>();

        @EventHandler
        public void onInventoryInteraction(InventoryClickEvent event) {

            if (!SharedShopElements.sellMenuNullPointPreventer(event)) return;
            if (!EliteMenu.isEliteMenu(event, menus) || !EliteMenu.isTopMenu(event)) return;
            event.setCancelled(true);

            //info button
            if (event.getCurrentItem().equals(BuyOrSellMenuConfig.INFORMATION_ITEM)) {
                return;
            }

            //buy custom items button
            if (event.getCurrentItem().getItemMeta().getDisplayName().equals(BuyOrSellMenuConfig.BUY_CUSTOM_ITEM.getItemMeta().getDisplayName())) {
                CustomShopMenu.customShopConstructor((Player) event.getWhoClicked());
                menus.remove((Player) event.getWhoClicked());
                return;
            }

            //buy procedural items button
            if (event.getCurrentItem().getItemMeta().getDisplayName().equals(BuyOrSellMenuConfig.BUY_PROCEDURAL_ITEM.getItemMeta().getDisplayName())) {
                ProceduralShopMenu.shopConstructor((Player) event.getWhoClicked());
                menus.remove((Player) event.getWhoClicked());
                return;
            }

            //sell items button
            if (event.getCurrentItem().getItemMeta().getDisplayName().equals(BuyOrSellMenuConfig.SELL_ITEM.getItemMeta().getDisplayName())) {
                SellMenu sellMenu = new SellMenu();
                sellMenu.constructSellMenu((Player) event.getWhoClicked());
                menus.remove((Player) event.getWhoClicked());
            }

        }

    }

}
