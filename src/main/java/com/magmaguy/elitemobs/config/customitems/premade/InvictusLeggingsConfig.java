package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class InvictusLeggingsConfig extends CustomItemsConfigFields {
    public InvictusLeggingsConfig() {
        super("invictus_leggings", true, Material.NETHERITE_LEGGINGS, "<g:#FFD700:#FF4500>Invictus Leggings</g>", new ArrayList<>(List.of("&2Awarded to the champions of the", "&2Wood League Arena!")));
        setEnchantments(new ArrayList<>(List.of("PROTECTION,5", "BLAST_PROTECTION,4", "PROJECTILE_PROTECTION,4", "MENDING,1", "UNBREAKING,5")));
        setPotionEffects(List.of("JUMP,0,self,continuous"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(50);
    }
}
