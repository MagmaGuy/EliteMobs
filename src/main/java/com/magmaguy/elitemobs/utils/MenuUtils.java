package com.magmaguy.elitemobs.utils;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuUtils {

    public static boolean isValidMenu(InventoryClickEvent event, String menuName) {
        if (!event.getView().getTitle().equals(menuName)) return false;
        return isValidMenu(event);
    }

    public static boolean isValidMenu(InventoryClickEvent event) {
        if (!event.getWhoClicked().getType().equals(EntityType.PLAYER)) return false;
        if (event.getCurrentItem() == null) return false;
        return !event.getCurrentItem().getType().equals(Material.AIR);
    }

}
