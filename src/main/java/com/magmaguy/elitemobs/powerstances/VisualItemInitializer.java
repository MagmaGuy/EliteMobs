package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class VisualItemInitializer {

    public static Item intializeItem(ItemStack itemStack, Location location) {

        Item item = location.getWorld().dropItem(location, itemStack);
        item.setPickupDelay(Integer.MAX_VALUE);
        MetadataHandler.registerMetadata(item, MetadataHandler.VISUAL_EFFECT_MD, 0);
        MetadataHandler.registerMetadata(item, MetadataHandler.BETTERDROPS_COMPATIBILITY_MD, true);
        if (!VersionChecker.currentVersionIsUnder(1, 11))
            item.setGravity(false);
        item.setInvulnerable(true);

        return item;

    }

}
