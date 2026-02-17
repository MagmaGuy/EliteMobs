package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.menus.premade.EliteScrollMenuConfig;
import com.magmaguy.elitemobs.items.EliteScroll;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EliteScrollMenu extends EliteMenu {

    public EliteScrollMenu(Player player) {
        String name = EliteScrollMenuConfig.getMenuName();
        if (DefaultConfig.useResourcePackModels())
            name = ChatColor.WHITE + "\uDB83\uDEF1\uDB83\uDE01\uDB83\uDEF5           " + name;
        Inventory inventory = Bukkit.createInventory(player, 54, name);

        inventory.setItem(EliteScrollMenuConfig.getInfoSlot(), EliteScrollMenuConfig.getInfoButton());
        inventory.setItem(EliteScrollMenuConfig.getNonEliteItemInfoSlot(), EliteScrollMenuConfig.getNonEliteItemInfoButton());
        inventory.setItem(EliteScrollMenuConfig.getEliteScrollItemInfoSlot(), EliteScrollMenuConfig.getEliteScrollItemInfoButton());
        inventory.setItem(EliteScrollMenuConfig.getOutputInfoSlot(), EliteScrollMenuConfig.getOutputInfoButton());
        inventory.setItem(EliteScrollMenuConfig.getCancelSlot(), EliteScrollMenuConfig.getCancelButton());
        inventory.setItem(EliteScrollMenuConfig.getConfirmSlot(), EliteScrollMenuConfig.getConfirmButton());

        player.openInventory(inventory);
        EliteScrollMenuEvents.menus.add(inventory);
    }

    private static void updateMenu(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getItem(EliteScrollMenuConfig.getEliteScrollItemSlot()) == null ||
                event.getView().getTopInventory().getItem(EliteScrollMenuConfig.getNonEliteItemSlot()) == null) {
            event.getView().getTopInventory().setItem(EliteScrollMenuConfig.getOutputSlot(), null);
           return;
        }
        event.getView().getTopInventory().setItem(EliteScrollMenuConfig.getOutputSlot(),
                EliteScroll.convertVanillaItem(
                        event.getView().getTopInventory().getItem(EliteScrollMenuConfig.getNonEliteItemSlot()),
                        event.getView().getTopInventory().getItem(EliteScrollMenuConfig.getEliteScrollItemSlot())));
    }

    public static class EliteScrollMenuEvents implements Listener {
        private static final Set<Inventory> menus = new HashSet<>();

        public static void shutdown() {
            menus.clear();
        }

        @EventHandler(ignoreCancelled = true)
        public void onInventoryInteract(InventoryClickEvent event) {
            if (!EliteMenu.isEliteMenu(event, menus)) return;
            event.setCancelled(true);
            if (!SharedShopElements.itemNullPointerPrevention(event)) return;

            if (isTopMenu(event)) {
                handleTopInventory(event);
            } else {
                handleBottomInventory(event);
            }

            updateMenu(event);
        }

        private void handleTopInventory(InventoryClickEvent event) {
            int clickedSlot = event.getSlot();

            if (clickedSlot == EliteScrollMenuConfig.getCancelSlot()) cancel(event);
            else if (clickedSlot == EliteScrollMenuConfig.getConfirmSlot()) confirm(event);
            else if (clickedSlot == EliteScrollMenuConfig.getEliteScrollItemSlot() ||
                    clickedSlot == EliteScrollMenuConfig.getNonEliteItemSlot()) {
                moveItemDown(event.getView().getTopInventory(), clickedSlot, event.getWhoClicked());
                event.getView().getTopInventory().clear(clickedSlot);
            }
        }

        private void handleBottomInventory(InventoryClickEvent event) {
//            if (EliteItemManager.isEliteMobsItem(event.getCurrentItem())) return;
            if (EliteScroll.isEliteScroll(event.getCurrentItem())) {
                moveOneItemUp(EliteScrollMenuConfig.getEliteScrollItemSlot(), event);
            }
            else if (!EliteItemManager.isEliteMobsItem(event.getCurrentItem())){
                moveOneItemUp(EliteScrollMenuConfig.getNonEliteItemSlot(), event);
            }
        }

        private void confirm(InventoryClickEvent event) {
            moveItemDown(event.getView().getTopInventory(), EliteScrollMenuConfig.getOutputSlot(), event.getWhoClicked());
            event.getView().getTopInventory().clear();
            event.getWhoClicked().closeInventory();
        }

        private void cancel(InventoryClickEvent event) {
            event.getWhoClicked().closeInventory();
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            if (menus.contains(event.getInventory())) {
                menus.remove(event.getInventory());
                EliteMenu.cancel(event.getPlayer(), event.getView().getTopInventory(), event.getView().getBottomInventory(),
                        new ArrayList<>(List.of(EliteScrollMenuConfig.getEliteScrollItemSlot(), EliteScrollMenuConfig.getNonEliteItemSlot())));
            }
        }

    }
}
