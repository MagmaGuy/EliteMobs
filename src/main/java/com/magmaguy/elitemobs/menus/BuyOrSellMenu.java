package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CustomModelsConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.menus.premade.BuyOrSellMenuConfig;
import com.magmaguy.elitemobs.utils.CustomModelAdder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Set;

import static com.magmaguy.elitemobs.menus.BuyOrSellMenu.BuyOrSellMenuEvents.menus;

public class BuyOrSellMenu {

    public static void constructBuyOrSellMenu(Player player, ItemStack buyItemStack) {
        String inventoryName = BuyOrSellMenuConfig.SHOP_NAME;
        if (DefaultConfig.useResourcePackModels())
            inventoryName = ChatColor.WHITE + "\uDB83\uDEF1\uDB83\uDE07\uDB83\uDEF5       " + inventoryName;

        Inventory shopInventory = Bukkit.createInventory(player, 18, inventoryName);
        menus.add(shopInventory);
        //information item
        ItemStack info = BuyOrSellMenuConfig.INFORMATION_ITEM;
        if (DefaultConfig.useResourcePackModels()) {
            info.setType(Material.PAPER);
            ItemMeta itemMeta = info.getItemMeta();
            itemMeta.setCustomModelData(MetadataHandler.signatureID);
            info.setItemMeta(itemMeta);
            CustomModelAdder.addCustomModel(info, CustomModelsConfig.goldenQuestionMark);
        }

        shopInventory.setItem(BuyOrSellMenuConfig.INFORMATION_SLOT, info);
        //sell item
        shopInventory.setItem(BuyOrSellMenuConfig.SELL_SLOT, BuyOrSellMenuConfig.SELL_ITEM);
        //buy item
        shopInventory.setItem(BuyOrSellMenuConfig.BUY_SLOT, buyItemStack);
        player.openInventory(shopInventory);
    }

    public static class BuyOrSellMenuEvents implements Listener {
        public static final Set<Inventory> menus = new HashSet<>();

        public static void shutdown() {
            menus.clear();
        }

        @EventHandler
        public void onInventoryInteraction(InventoryClickEvent event) {

            if (!EliteMenu.isEliteMenu(event, menus)) return;
            event.setCancelled(true);
            if (!SharedShopElements.itemNullPointerPrevention(event)) return;

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

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            menus.remove(event.getInventory());
        }

    }

}
