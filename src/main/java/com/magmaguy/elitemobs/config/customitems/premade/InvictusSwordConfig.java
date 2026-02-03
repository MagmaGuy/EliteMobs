package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class InvictusSwordConfig extends CustomItemsConfigFields {
    public InvictusSwordConfig() {
        super("invictus_sword", true, Material.NETHERITE_SWORD, "<g:#FFD700:#FF4500>Invictus Sword</g>", new ArrayList<>(List.of("&2Awarded to the champions of the", "&2Wood League Arena!")));
        setEnchantments(new ArrayList<>(List.of("SHARPNESS,5", "KNOCKBACK,2", "MENDING,1", "LIGHTNING,3", "SWEEPING_EDGE,1", "UNBREAKING,5")));
        setPotionEffects(List.of("WITHER,0,target,onHit"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(50);
    }
}
