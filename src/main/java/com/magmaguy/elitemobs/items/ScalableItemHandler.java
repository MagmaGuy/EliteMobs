package com.magmaguy.elitemobs.items;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public class ScalableItemHandler {

    /*
    Fully scalable items can be of any tier up to the maximum tier
    The hashmap list stores the regular enchantments on [0] and the custom enchantments on [1}
     */
    public static HashMap<ItemStack, List<HashMap>> fullyScalableCustomItems = new HashMap();
    public static HashMap<ItemStack, List<HashMap>> fullyScalableUniqueItems = new HashMap();

    /*
    Limited scalable items can be of any tier up to the defined configuration presets
     */
    public static HashMap<ItemStack, List<HashMap>> limitedScalableCustomItems = new HashMap();
    public static HashMap<ItemStack, List<HashMap>> limitedScalableUniqueItems = new HashMap();

    public ItemStack generateItem(){



    }

}
