package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class GruntsSwordConfig extends CustomItemsConfigFields {
    public GruntsSwordConfig() {
        super("grunts_sword", true, Material.IRON_SWORD, "&fGrunt's Sword", Arrays.asList("&2Awarded to those who challenge the", "&2Wood League Arena!"));
        setEnchantments(Arrays.asList("SHARPNESS,5", "KNOCKBACK,2", "SWEEPING_EDGE,1", "UNBREAKING,5"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(20);
    }
}
