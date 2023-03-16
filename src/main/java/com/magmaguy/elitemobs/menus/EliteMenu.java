package com.magmaguy.elitemobs.menus;

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
        return inventories.contains(event.getInventory());
    }

    public static boolean isTopMenu(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return false;
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

    public static void moveOneItemUp(int targetSlot, InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        ItemStack newItemStack = event.getCurrentItem().clone();
        newItemStack.setAmount(1);
        event.getView().getTopInventory().setItem(targetSlot, newItemStack);
        event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() - 1);
    }

    //Returns true if there was an item loss
    public static boolean moveItemDown(Inventory inventoryToClear, int slotToClear, HumanEntity player, boolean canDrop) {
        boolean itemLoss = false;
        if (inventoryToClear.getItem(slotToClear) == null) return itemLoss;
        HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(inventoryToClear.getItem(slotToClear));
        //Item is ultimately lost if the inventory is full and it can't be dropped
        if (!leftovers.isEmpty() && canDrop)
            player.getWorld().dropItem(player.getLocation(), inventoryToClear.getItem(slotToClear));
        else if (!leftovers.isEmpty())
            itemLoss = true;
        inventoryToClear.setItem(slotToClear, null);
        return itemLoss;
    }

    public static void moveItemDown(Inventory inventoryToClear, int slotToClear, HumanEntity player) {
        moveItemDown(inventoryToClear, slotToClear, player, true);
    }

}
