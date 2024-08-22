package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class ChallengersHelmetConfig extends CustomItemsConfigFields {
    public ChallengersHelmetConfig() {
        super("challengers_helmet", true, Material.DIAMOND_HELMET, "&cChallenger's Helmet", new ArrayList<>(List.of("&2Awarded to those who challenge the", "&2Wood League Arena!")));
        setEnchantments(new ArrayList<>(List.of("PROTECTION,5", "BLAST_PROTECTION,4", "PROJECTILE_PROTECTION,4", "MENDING,1", "UNBREAKING,5")));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(40);
    }
}
