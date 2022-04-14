package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class VeteransLeggingsConfig extends CustomItemsConfigFields {
    public VeteransLeggingsConfig(){
        super("veterans_leggings", true, Material.GOLDEN_LEGGINGS, "&6Veteran's Leggings", Arrays.asList("&2Awarded to those who challenge the", "&2Wood League Arena!"));
        setEnchantments(Arrays.asList("PROTECTION_ENVIRONMENTAL,30", "PROTECTION_PROJECTILE,15", "MENDING,1", "DURABILITY,5"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
