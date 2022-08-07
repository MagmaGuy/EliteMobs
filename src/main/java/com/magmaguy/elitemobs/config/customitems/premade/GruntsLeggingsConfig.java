package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class GruntsLeggingsConfig extends CustomItemsConfigFields {
    public GruntsLeggingsConfig(){
        super("grunts_leggings", true, Material.IRON_LEGGINGS, "&fGrunt's Leggings", Arrays.asList("&2Awarded to those who challenge the", "&2Wood League Arena!"));
        setEnchantments(Arrays.asList("PROTECTION_ENVIRONMENTAL,5", "PROTECTION_PROJECTILE,4", "DURABILITY,5"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(20);
    }
}
