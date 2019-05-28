package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.entity.Player;

/**
 * Created by MagmaGuy on 01/05/2017.
 */
public class GetLootCommandHandler {

    public static boolean getLoot(Player player, String args1) {
        CustomItem customItem = CustomItem.getCustomItem(args1);
        if (customItem == null) return false;
        player.getInventory().addItem(customItem.generateDefaultsItemStack());
        return true;
    }

}
