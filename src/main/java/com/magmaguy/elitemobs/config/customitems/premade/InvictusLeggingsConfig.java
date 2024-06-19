package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class InvictusLeggingsConfig extends CustomItemsConfigFields {
    public InvictusLeggingsConfig() {
        super("invictus_leggings", true, Material.NETHERITE_LEGGINGS, "&4Invictus Leggings", Arrays.asList("&2Awarded to the champions of the", "&2Wood League Arena!"));
        setEnchantments(Arrays.asList("PROTECTION,5", "BLAST_PROTECTION,4", "PROJECTILE_PROTECTION,4", "MENDING,1", "UNBREAKING,5"));
        setPotionEffects(List.of("JUMP,0,self,continuous"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(50);
    }
}
