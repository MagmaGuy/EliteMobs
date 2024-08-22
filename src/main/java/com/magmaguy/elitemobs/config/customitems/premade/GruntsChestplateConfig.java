package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class GruntsChestplateConfig extends CustomItemsConfigFields {
    public GruntsChestplateConfig() {
        super("grunts_chestplate", true, Material.IRON_CHESTPLATE, "&fGrunt's Chestplate", new ArrayList<>(List.of("&2Awarded to those who challenge the", "&2Wood League Arena!")));
        setEnchantments(new ArrayList<>(List.of("PROTECTION,5", "PROJECTILE_PROTECTION,4", "UNBREAKING,5")));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(20);
    }
}
