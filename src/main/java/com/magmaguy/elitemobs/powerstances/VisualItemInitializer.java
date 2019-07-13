package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.EntityTracker;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class VisualItemInitializer {

    public static Item initializeItem(ItemStack itemStack, Location location) {
        Item item = location.getWorld().dropItem(location, itemStack);
        item.setPickupDelay(Integer.MAX_VALUE);
        EntityTracker.registerItemVisualEffects(item);
        item.setGravity(false);
        item.setInvulnerable(true);

        return item;
    }

}
