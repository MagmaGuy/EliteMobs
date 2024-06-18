package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class VeteransHelmetConfig extends CustomItemsConfigFields {
    public VeteransHelmetConfig() {
        super("veterans_helmet", true, Material.GOLDEN_HELMET, "&6Veteran's Helmet", Arrays.asList("&2Awarded to those who challenge the", "&2Wood League Arena!"));
        setEnchantments(Arrays.asList("PROTECTION,5", "PROTECTION_PROJECTILE,4", "MENDING,1", "UNBREAKING,5"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(30);
    }
}
