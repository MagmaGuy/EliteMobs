package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class ChallengersLeggingsConfig extends CustomItemsConfigFields {
    public ChallengersLeggingsConfig() {
        super("challengers_leggings", true, Material.DIAMOND_LEGGINGS, "&cChallenger's Leggings", Arrays.asList("&2Awarded to those who challenge the", "&2Wood League Arena!"));
        setEnchantments(Arrays.asList("PROTECTION,5", "BLAST_PROTECTION,4", "PROJECTILE_PROTECTION,4", "MENDING,1", "UNBREAKING,5"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(40);
    }
}
