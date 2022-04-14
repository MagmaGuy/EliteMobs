package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class NovicesLeggingsConfig extends CustomItemsConfigFields {
    public NovicesLeggingsConfig(){
        super("novices_leggings", true, Material.LEATHER_LEGGINGS, "&8Novice's Leggings", Arrays.asList("&2Awarded to those who challenge the", "&2Wood League Arena!"));
        setEnchantments(Arrays.asList("PROTECTION_ENVIRONMENTAL,10", "DURABILITY,5"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
