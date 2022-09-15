package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class VeteransSwordConfig extends CustomItemsConfigFields {
    public VeteransSwordConfig() {
        super("veterans_sword", true, Material.GOLDEN_SWORD, "&6Veteran's Sword", Arrays.asList("&2Awarded to those who challenge the", "&2Wood League Arena!"));
        setEnchantments(Arrays.asList("DAMAGE_ALL,5", "KNOCKBACK,2", "MENDING,1", "SWEEPING_EDGE,1", "DURABILITY,5"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(30);
    }
}
