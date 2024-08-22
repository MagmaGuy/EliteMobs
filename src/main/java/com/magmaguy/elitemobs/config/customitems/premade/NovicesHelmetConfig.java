package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class NovicesHelmetConfig extends CustomItemsConfigFields {
    public NovicesHelmetConfig() {
        super("novices_helmet", true, Material.LEATHER_HELMET, "&8Novice's Helmet", new ArrayList<>(List.of("&2Awarded to those who challenge the", "&2Wood League Arena!")));
        setEnchantments(new ArrayList<>(List.of("PROTECTION,5", "UNBREAKING,5")));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(10);
    }
}
