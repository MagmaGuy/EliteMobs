package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class VeteransBootsConfig extends CustomItemsConfigFields {
    public VeteransBootsConfig(){
        super("veterans_boots", true, Material.GOLDEN_BOOTS, "&6Veteran's Boots", Arrays.asList("&2Awarded to those who challenge the", "&2Wood League Arena!"));
        setEnchantments(Arrays.asList("PROTECTION_ENVIRONMENTAL,5", "PROTECTION_PROJECTILE,4", "MENDING,1", "DURABILITY,5"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(30);
    }
}
