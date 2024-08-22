package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class VeteransSwordConfig extends CustomItemsConfigFields {
    public VeteransSwordConfig() {
        super("veterans_sword", true, Material.GOLDEN_SWORD, "&6Veteran's Sword", new ArrayList<>(List.of("&2Awarded to those who challenge the", "&2Wood League Arena!")));
        setEnchantments(new ArrayList<>(List.of("SHARPNESS,5", "KNOCKBACK,2", "MENDING,1", "SWEEPING_EDGE,1", "UNBREAKING,5")));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(30);
    }
}
