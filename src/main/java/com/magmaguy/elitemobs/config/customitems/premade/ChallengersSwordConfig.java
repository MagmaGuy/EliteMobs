package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class ChallengersSwordConfig extends CustomItemsConfigFields {
    public ChallengersSwordConfig() {
        super("challengers_sword", true, Material.DIAMOND_SWORD, "<g:#DC143C:#FF6347>Challenger's Sword</g>", new ArrayList<>(List.of("&2Awarded to those who challenge the", "&2Wood League Arena!")));
        setEnchantments(new ArrayList<>(List.of("SHARPNESS,5", "KNOCKBACK,2", "MENDING,1", "LIGHTNING,3", "SWEEPING_EDGE,1", "UNBREAKING,5")));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(40);
    }
}
