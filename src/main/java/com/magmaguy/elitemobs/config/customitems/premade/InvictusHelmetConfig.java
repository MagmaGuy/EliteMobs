package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class InvictusHelmetConfig extends CustomItemsConfigFields {
    public InvictusHelmetConfig() {
        super("invictus_helmet", true, Material.NETHERITE_HELMET, "&4Invictus Helmet", Arrays.asList("&2Awarded to the champions of the", "&2Wood League Arena!"));
        setEnchantments(Arrays.asList("PROTECTION,5", "BLAST_PROTECTION,4", "PROJECTILE_PROTECTION,4", "MENDING,1", "UNBREAKING,5"));
        setPotionEffects(List.of("NIGHT_VISION,0,self,continuous"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(50);
    }
}
