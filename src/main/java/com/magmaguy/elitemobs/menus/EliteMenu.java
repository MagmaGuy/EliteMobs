package com.magmaguy.elitemobs.menus;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class EliteMenu implements Listener {

    public static void createEliteMenu(Inventory inventory, Set<Inventory> inventories) {
        inventories.add(inventory);
    }

    public static boolean isEliteMenu(InventoryClickEvent event, Set<Inventory> inventories) {
        if (event.getCurrentItem() == null) return false;
        if (event.getCurrentItem().getType().equals(Material.AIR)) return false;
        return inventories.contains(event.getInventory());
    }

    public static boolean isTopMenu(InventoryClickEvent event) {
        return event.getClickedInventory().equals(event.getView().getTopInventory());
    }

    public static boolean isBottomMenu(InventoryClickEvent event) {
        return !isTopMenu(event);
    }

    public static void cancel(HumanEntity player, Inventory eliteInventory, Inventory playerInventory, List<Integer> inputSlots) {
        for (int slot : inputSlots) {
            if (eliteInventory.getItem(slot) != null) {
                HashMap<Integer, ItemStack> leftOvers = playerInventory.addItem(eliteInventory.getItem(slot));
                eliteInventory.clear(slot);
                if (!leftOvers.isEmpty())
                    leftOvers.values().forEach(itemStack -> player.getWorld().dropItem(player.getLocation(), itemStack));
            }
        }
    }

}
