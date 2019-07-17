package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.items.ItemTagger;
import org.bukkit.inventory.ItemStack;

public class EliteMobsItemDetector {

    public static boolean isEliteMobsItem(ItemStack itemStack) {
        return ItemTagger.isEliteItem(itemStack);
    }

}
