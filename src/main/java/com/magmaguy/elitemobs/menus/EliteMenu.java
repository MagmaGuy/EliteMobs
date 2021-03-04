package com.magmaguy.elitemobs.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

public class EliteMenu implements Listener {

    public static void createEliteMenu(Player player, Inventory inventory, HashMap<Player, Inventory> inventories) {
        inventories.put(player, inventory);
    }

    public static boolean isEliteMenu(InventoryClickEvent event, HashMap<Player, Inventory> inventories) {
        if (event.getCurrentItem() == null) return false;
        if (event.getCurrentItem().getType().equals(Material.AIR)) return false;
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = inventories.get(player);
        if (inventory == null) return false;
        return inventory.equals(event.getView().getTopInventory());
    }

    public static boolean isTopMenu(InventoryClickEvent event) {
        return event.getClickedInventory().equals(event.getView().getTopInventory());
    }

    public boolean isBottomMenu(InventoryClickEvent event) {
        return !isTopMenu(event);
    }

    public static boolean onInventoryClose(InventoryCloseEvent event, HashMap<Player, Inventory> inventories) {
        Player player = (Player) event.getPlayer();
        Inventory inventory = inventories.get(player);
        if (inventory == null) return false;
        inventories.remove(player);
        return event.getView().getTopInventory().equals(inventory);
    }

    public static void onPlayerQuit(PlayerQuitEvent event, HashMap<Player, Inventory> inventories) {
        inventories.remove(event.getPlayer());
    }

}
