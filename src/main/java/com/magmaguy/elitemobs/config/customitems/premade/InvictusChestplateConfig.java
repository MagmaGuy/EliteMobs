package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class InvictusChestplateConfig extends CustomItemsConfigFields {
    public InvictusChestplateConfig() {
        super("invictus_chestplate", true, Material.NETHERITE_CHESTPLATE, "&4Invictus Chestplate", new ArrayList<>(List.of("&2Awarded to the champions of the", "&2Wood League Arena!")));
        setEnchantments(new ArrayList<>(List.of("PROTECTION,5", "BLAST_PROTECTION,4", "PROJECTILE_PROTECTION,4", "MENDING,1", "UNBREAKING,5")));
        setPotionEffects(List.of("HEAL,0,self,onHit"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(50);
    }
}
