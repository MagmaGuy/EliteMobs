package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class VisualItemInitializer {

    public static Item intializeItem(ItemStack itemStack, Location location) {

        Item item = location.getWorld().dropItem(location, itemStack);
        item.setPickupDelay(Integer.MAX_VALUE);
        item.setMetadata(MetadataHandler.VISUAL_EFFECT_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, 0));
        item.setMetadata(MetadataHandler.BETTERDROPS_COMPATIBILITY_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        item.setGravity(false);
        item.setInvulnerable(true);

        return item;

    }

}
